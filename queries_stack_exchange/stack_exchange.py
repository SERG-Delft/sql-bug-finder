import time
import html
import pickle
import datetime
import requests
import mysql.connector


class StackExchange:
    def __init__(self, tag, start_page, end_page, load=False):
        self.tag, self.start_page, self.end_page, self.load = tag, start_page, end_page, load
        self.queries, self.sql_keywords = list(), list()
        self.db, self.add_owner, self.add_question, self.add_answer, self.add_query, self.add_page, self.get_page_q = self.get_db()
        self.req = 0
        self.run()

    @staticmethod
    def log(message):
        _now = datetime.datetime.now()
        print(f'[{_now}] {message}')

    @staticmethod
    def store_pickle(name, data):
        file = open(rf'./pickles/{name}.pkl', 'wb')
        pickle.dump(data, file)
        file.close()

    @staticmethod
    def load_pickle(name):
        file = open(rf'./pickles/{name}.pkl', 'rb')
        data = pickle.load(file)
        file.close()
        return data

    @staticmethod
    def get_db():
        db = mysql.connector.connect(
            host='localhost',
            user='root',
            password='root',
            db='homedb'
        )
        add_owner = f'''
            INSERT IGNORE INTO owners (user_id, user_type, reputation, accept_rate, display_name, link) 
            VALUES (%s, %s, %s, %s, %s, %s)
        '''
        add_question = f'''
            INSERT IGNORE INTO questions (
                question_id, user_id, is_answered, view_count, protected_date, accepted_answer_id, answer_count, score,
                last_activity_date, last_edit_date, creation_date, link, title
            ) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        '''
        add_answer = f'''
            INSERT IGNORE INTO answers (
                answer_id, question_id, user_id, is_accepted, score, last_activity_date, last_edit_date, creation_date
            ) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        '''
        add_query = f'''
            INSERT IGNORE INTO queries (
                answer_id, question_id, query, is_valid
            ) 
            VALUES (%s, %s, %s, %s)
        '''
        add_page = f'''
            INSERT IGNORE INTO pages (
                page_id, is_parsed
            ) 
            VALUES (%s, true)
        '''
        get_page_q = f'''
            SELECT * FROM pages WHERE page_id = %s
        '''
        return db, add_owner, add_question, add_answer, add_query, add_page, get_page_q

    def store_data(self, owners, questions, answers, queries, page):
        self.log(f'\t\t --> adding new data to db')
        cursor = self.db.cursor()
        self.log(f'\t\t --> adding new owners to db')
        cursor.executemany(self.add_owner, owners)
        self.log(f'\t\t --> done adding new owners to db (rows: {cursor.rowcount})')
        self.log(f'\t\t --> adding new questions to db')
        cursor.executemany(self.add_question, questions)
        self.log(f'\t\t --> done adding new questions to db (rows: {cursor.rowcount})')
        self.log(f'\t\t --> adding new answers to db')
        cursor.executemany(self.add_answer, answers)
        self.log(f'\t\t --> done adding new answers to db (rows: {cursor.rowcount})')
        self.log(f'\t\t --> adding new queries to db')
        cursor.executemany(self.add_query, queries)
        self.log(f'\t\t --> done adding new queries to db (rows: {cursor.rowcount})')
        self.log(f'\t\t --> adding new page to db')
        cursor.execute(self.add_page, (page,))
        self.log(f'\t\t --> done adding new page to db (rows: {cursor.rowcount})')
        self.log(f'\t\t --> committing changes to db')
        self.db.commit()
        self.log(f'\t\t --> done committing all changes to db')
        self.log(f'\t\t --> done adding new data to db')

    def write_queries_to_file(self):
        with open('queries.txt', 'w') as file:
            for query in self.queries:
                try:
                    # discard small queries which are most probably parsing artefacts
                    if len(query) > 15:
                        file.write('%s\n' % query)
                except:
                    continue

    def run(self):
        self.sql_keywords = self.load_pickle('sql_keywords')
        self.get_data()
        # self.write_queries_to_file()

    def load_data(self):
        self.sql_keywords = self.load_pickle('sql_keywords')
        if not self.load:
            self.get_data()
        for index in range(self.start_page, self.end_page + 1):
            self.queries.extend(self.load_pickle(f'queries/queries_{index}'))
        print(f'loaded {len(self.sql_keywords)} sql keywords')
        print(f'loaded {len(self.queries)} queries')

    def get_data(self):
        self.log(f'collecting data for pages {self.start_page} up to {self.end_page}')
        page_index = self.start_page - 1
        cursor = self.db.cursor()
        for page in range(self.start_page, self.end_page + 1):
            page_index += 1
            owners, questions, answers, queries = list(), list(), list(), list()
            cursor.execute(self.get_page_q, (page,))
            if cursor.fetchone():
                self.log(f'\t --> found queries for page #{page_index}, skipping')
                continue
            res = self.get_page(page)
            self.log(f'\t --> collecting queries for page #{page}')
            for item in res['items']:
                owners.append(self.get_owners(item['owner']))
                questions.append(self.get_questions(item))
                queries.extend(self.get_queries(item['question_id'], -1, item['body']))
                if item.get('answers') is not None:
                    for answer in item['answers']:
                        owners.append(self.get_owners(answer['owner']))
                        answers.append(self.get_answers(answer))
                        queries.extend(self.get_queries(item['question_id'], answer['answer_id'], answer['body']))
            self.store_data(owners, questions, answers, queries, page)
            self.log(f'\t --> done collecting queries for page #{page}')
        self.log(f'done collecting data for all pages')

    def get_owners(self, owner):
        return (
            owner.get('user_id', -1),
            owner.get('user_type', ''),
            owner.get('reputation', -1),
            owner.get('accept_rate', -1),
            owner.get('display_name', ''),
            owner.get('link', '')
        )

    def get_questions(self, question):
        return (
            question.get('question_id', -1),
            question.get('owner').get('user_id', -1),
            question.get('is_answered', ''),
            question.get('view_count', -1),
            datetime.datetime.fromtimestamp(question.get('protected_date', 0)),
            question.get('accepted_answer_id', -1),
            question.get('answer_count', -1),
            question.get('score', -1),
            datetime.datetime.fromtimestamp(question.get('last_activity_date', 0)),
            datetime.datetime.fromtimestamp(question.get('last_edit_date', 0)),
            datetime.datetime.fromtimestamp(question.get('creation_date', 0)),
            question.get('link', ''),
            question.get('title', '')
        )

    def get_answers(self, answer):
        return (
            answer.get('answer_id', -1),
            answer.get('question_id', -1),
            answer.get('owner').get('user_id', -1),
            answer.get('is_accepted', ''),
            answer.get('score', -1),
            datetime.datetime.fromtimestamp(answer.get('last_activity_date', 0)),
            datetime.datetime.fromtimestamp(answer.get('last_edit_date', 0)),
            datetime.datetime.fromtimestamp(answer.get('creation_date', 0))
        )

    def get_queries(self, question_id, answer_id, body):
        code_snippets = list()
        if body.find('<code>') != -1:
            for snippet in body.split('<code>'):
                if snippet.find('</code>') != -1:
                    code_snippets.append(html.unescape(snippet.split('</code>')[0].strip().lower()))
        _queries = self.get_queries_from_code_snippets(code_snippets)
        queries = list()
        for query in _queries:
            # discard small queries which are most probably parsing artefacts
            if len(query) > 15:
                queries.append((answer_id, question_id, query, ''))
        return queries

    def get_page(self, page):
        if self.req >= 30:
            self.req = 0
            self.log(f'\t --> self throttling requests (max 30req/sec) waiting 1s')
            time.sleep(1)
        self.log(f'\t --> requesting questions and answers for page #{page}')
        url = f'https://api.stackexchange.com/' \
              f'2.2/questions?page={page}&pagesize=50&order=desc&sort=votes&tagged={self.tag}&' \
              f'site=stackoverflow&filter=!*0Wr)(zyHwbL2nnCxstd(.pH8Vt4yyy8N*hhAOSKT&key=ajG)5rXk5XTERT4xyjq9HA(('
        req = requests.get(url=url)
        res = req.json()
        if res.get('backoff') is not None:
            print(f'backing off for {res["backoff"]}s')
            time.sleep(int(res['backoff']))
        quota_remaining = ''
        if res.get('quota_remaining') is not None:
            quota_remaining = res['quota_remaining']
        self.log(f'\t --> done requesting questions and answers for page #{page} (quota_remaining: {quota_remaining})')
        self.req += 1
        return res

    def get_queries_from_code_snippets(self, code_snippets):
        queries = list()
        for snippet in code_snippets:
            snippet_lines = snippet.split('\n')
            snippet_sql_lines = list()
            for i in range(len(snippet_lines)):
                snippet_lines[i] = snippet_lines[i].strip()
                if self.is_sql_statement(snippet_lines[i]) or self.previous_line_special_sql(i, snippet_lines):
                    snippet_sql_lines.append(snippet_lines[i])
            queries.extend(self.build_sql_queries(snippet_sql_lines))
        return queries

    def is_sql_statement(self, snippet):
        if snippet in ['(', ')', ');']:
            return True
        for keyword in self.sql_keywords:
            if snippet.startswith(keyword) and snippet.split(' ')[0] == keyword.split(' ')[0]:
                return True
        return False

    def previous_line_special_sql(self, i, lines):
        if i == 0:
            return False
        return lines[i - 1] != '' and (
                lines[i - 1][-1] == ',' or
                lines[i - 1][-1] == '(' or
                self.is_exact_sql_keyword(lines[i - 1])
        )

    def is_exact_sql_keyword(self, snippet):
        for keyword in self.sql_keywords:
            if snippet == keyword:
                return True
        return False

    def build_sql_queries(self, snippet_sql_lines):
        queries = list()
        sql_query = ''
        for line in snippet_sql_lines:
            if line != '' and line[-1] == ';':
                if sql_query != '':
                    sql_query += ' ' + line
                    queries.append(sql_query)
                    sql_query = ''
                else:
                    queries.append(line)
            else:
                if sql_query == '':
                    sql_query += line
                else:
                    sql_query += ' ' + line
        if sql_query != '':
            queries.append(sql_query + ';')
        return queries


stack_exchange = StackExchange('sql', 1, 2000, load=False)
