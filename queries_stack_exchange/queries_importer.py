import os
from os import walk
import mysql.connector


class QueriesImporter:
    def __init__(self, path):
        self.db, self.add_query = self.get_db()
        self.path = path
        self.run()

    @staticmethod
    def get_db():
        db = mysql.connector.connect(
            host='localhost',
            user='root',
            password='root',
            db='homedb'
        )
        add_query = f'''
            INSERT IGNORE INTO evo_sql_queries (
                answer_id, question_id, query, is_valid
            ) 
            VALUES (%s, %s, %s, %s)
        '''
        return db, add_query

    def store_queries(self, queries):
        print('adding new data to db')
        cursor = self.db.cursor()
        cursor.executemany(self.add_query, queries)
        self.db.commit()

    def run(self):
        _, _, filenames = next(walk(self.path))
        print(filenames)
        for file in filenames:
            with open(os.path.join(self.path, file)) as f:
                queries = f.read().splitlines()
            queries = [(-1, -1, query + ';', '') for query in queries if not query.startswith('--')]
            print(f'collected: {len(queries)} queries from file: {file}')
            self.store_queries(queries)


QueriesImporter('./files')
