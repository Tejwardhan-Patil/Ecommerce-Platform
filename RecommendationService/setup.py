from setuptools import setup, find_packages
import os

# Function to read the content of the README file
def read(fname):
    return open(os.path.join(os.path.dirname(__file__), fname)).read()

setup(
    name="RecommendationService",
    version="1.0.0",
    author="Ecommerce Platform",
    author_email="support@website.com",
    description="A service for providing product recommendations based on user profiles and product data",
    long_description=read('README.md'),
    long_description_content_type="text/markdown",
    url="https://website.com/recommendationservice",
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    include_package_data=True,
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.7',
    install_requires=[
        "pandas>=1.3.0",
        "scikit-learn>=0.24.0",
        "numpy>=1.21.0",
        "Flask>=2.0.0",
        "SQLAlchemy>=1.4.0",
        "fastapi>=0.65.0",
        "uvicorn>=0.14.0",
        "gunicorn>=20.0.4",
        "PyYAML>=5.4",
        "pytest>=6.2.4",
        "pytest-cov>=2.12.0",
    ],
    entry_points={
        'console_scripts': [
            'recommendation_service=core.services.recommendation_service:main',
            'model_training_service=core.services.model_training_service:main',
        ],
    },
    data_files=[
        ('config', ['src/infrastructure/config/config.yaml']),
        ('datasets', [
            'src/data/datasets/user_data.csv',
            'src/data/datasets/product_data.csv'
        ]),
    ],
    extras_require={
        'dev': [
            'black',
            'flake8',
            'isort',
            'jupyter',
        ],
        'test': [
            'pytest',
            'pytest-cov',
        ],
    },
    zip_safe=False,
    test_suite='tests',
)