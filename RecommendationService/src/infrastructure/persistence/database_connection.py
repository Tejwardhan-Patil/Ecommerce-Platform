import os
import logging
import psycopg2
from psycopg2 import pool
from psycopg2.extras import RealDictCursor
from contextlib import contextmanager
import time

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DatabaseConnectionError(Exception):
    """Custom exception to handle database connection errors."""
    pass

class DatabaseConnection:
    _instance = None
    _connection_pool = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(DatabaseConnection, cls).__new__(cls)
        return cls._instance

    def __init__(self, minconn=1, maxconn=10):
        self.dbname = os.getenv('DB_NAME', 'recommendation_db')
        self.user = os.getenv('DB_USER', 'user')
        self.password = os.getenv('DB_PASSWORD', 'password')
        self.host = os.getenv('DB_HOST', 'localhost')
        self.port = os.getenv('DB_PORT', 5432)
        self.minconn = minconn
        self.maxconn = maxconn
        self._setup_connection_pool()

    def _setup_connection_pool(self):
        try:
            self._connection_pool = pool.SimpleConnectionPool(
                self.minconn, self.maxconn,
                database=self.dbname,
                user=self.user,
                password=self.password,
                host=self.host,
                port=self.port
            )
            if self._connection_pool:
                logger.info("Database connection pool created successfully.")
        except psycopg2.DatabaseError as e:
            logger.error(f"Error in creating the connection pool: {e}")
            raise DatabaseConnectionError(f"Failed to create connection pool: {e}")

    @contextmanager
    def get_connection(self):
        conn = None
        try:
            conn = self._connection_pool.getconn()
            if conn:
                logger.info("Connection acquired from the pool.")
            yield conn
        except psycopg2.DatabaseError as e:
            logger.error(f"Database connection error: {e}")
            raise DatabaseConnectionError(f"Failed to acquire connection: {e}")
        finally:
            if conn:
                self._connection_pool.putconn(conn)
                logger.info("Connection returned to the pool.")

    def close_all_connections(self):
        if self._connection_pool:
            self._connection_pool.closeall()
            logger.info("All database connections in the pool have been closed.")

class DatabaseOperations:
    def __init__(self):
        self.connection = DatabaseConnection()

    def execute_query(self, query, params=None):
        try:
            with self.connection.get_connection() as conn:
                with conn.cursor(cursor_factory=RealDictCursor) as cur:
                    cur.execute(query, params)
                    logger.info(f"Executed query: {query}")
                    conn.commit()
                    return cur.fetchall()
        except psycopg2.DatabaseError as e:
            logger.error(f"Failed to execute query: {e}")
            raise DatabaseConnectionError(f"Query execution failed: {e}")

    def execute_update(self, query, params=None):
        try:
            with self.connection.get_connection() as conn:
                with conn.cursor() as cur:
                    cur.execute(query, params)
                    logger.info(f"Executed update: {query}")
                    conn.commit()
        except psycopg2.DatabaseError as e:
            logger.error(f"Failed to execute update: {e}")
            raise DatabaseConnectionError(f"Update execution failed: {e}")

    def fetch_one(self, query, params=None):
        try:
            with self.connection.get_connection() as conn:
                with conn.cursor(cursor_factory=RealDictCursor) as cur:
                    cur.execute(query, params)
                    logger.info(f"Executed fetch one: {query}")
                    return cur.fetchone()
        except psycopg2.DatabaseError as e:
            logger.error(f"Failed to fetch one record: {e}")
            raise DatabaseConnectionError(f"Fetching one record failed: {e}")

    def fetch_all(self, query, params=None):
        try:
            with self.connection.get_connection() as conn:
                with conn.cursor(cursor_factory=RealDictCursor) as cur:
                    cur.execute(query, params)
                    logger.info(f"Executed fetch all: {query}")
                    return cur.fetchall()
        except psycopg2.DatabaseError as e:
            logger.error(f"Failed to fetch all records: {e}")
            raise DatabaseConnectionError(f"Fetching all records failed: {e}")

class RetryPolicy:
    def __init__(self, retries=3, delay=2, backoff=2):
        self.retries = retries
        self.delay = delay
        self.backoff = backoff

    def execute_with_retry(self, func, *args, **kwargs):
        retries = self.retries
        delay = self.delay

        while retries > 0:
            try:
                result = func(*args, **kwargs)
                return result
            except DatabaseConnectionError as e:
                logger.warning(f"Operation failed: {e}. Retrying in {delay} seconds...")
                time.sleep(delay)
                retries -= 1
                delay *= self.backoff

        raise DatabaseConnectionError("Operation failed after maximum retries.")

class DatabaseConnectionWithRetry:
    def __init__(self, retry_policy=None):
        self.db_ops = DatabaseOperations()
        self.retry_policy = retry_policy or RetryPolicy()

    def execute_query_with_retry(self, query, params=None):
        return self.retry_policy.execute_with_retry(self.db_ops.execute_query, query, params)

    def execute_update_with_retry(self, query, params=None):
        return self.retry_policy.execute_with_retry(self.db_ops.execute_update, query, params)

    def fetch_one_with_retry(self, query, params=None):
        return self.retry_policy.execute_with_retry(self.db_ops.fetch_one, query, params)

    def fetch_all_with_retry(self, query, params=None):
        return self.retry_policy.execute_with_retry(self.db_ops.fetch_all, query, params)

# Usage
if __name__ == "__main__":
    db = DatabaseConnectionWithRetry()

    # Query execution with retry
    try:
        query = "SELECT * FROM recommendations WHERE user_id = %s"
        user_id = 12345
        results = db.fetch_all_with_retry(query, (user_id,))
        logger.info(f"Recommendations for user {user_id}: {results}")
    except DatabaseConnectionError as e:
        logger.error(f"Database operation failed: {e}")