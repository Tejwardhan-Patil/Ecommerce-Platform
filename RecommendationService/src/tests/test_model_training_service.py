import unittest
from unittest.mock import patch, MagicMock
from src.core.services.model_training_service import ModelTrainingService
from src.data.data_preprocessing import DataPreprocessing
from src.data.feature_engineering import FeatureEngineering
from src.infrastructure.persistence.database_connection import DatabaseConnection
import pandas as pd

class TestModelTrainingService(unittest.TestCase):

    @patch('src.core.services.model_training_service.ModelTrainingService.load_data')
    @patch('src.core.services.model_training_service.ModelTrainingService.train_model')
    @patch('src.core.services.model_training_service.ModelTrainingService.evaluate_model')
    def test_model_training_workflow(self, evaluate_model, train_model, load_data):
        # Simulating the data loading process
        load_data.return_value = (pd.DataFrame({'user_id': [1, 2], 'product_id': [101, 102], 'rating': [5, 3]}),
                                  pd.DataFrame({'product_id': [101, 102], 'category': ['electronics', 'clothing']}))

        # Simulating the model training
        train_model.return_value = 'trained_model'

        # Simulating the model evaluation
        evaluate_model.return_value = {'accuracy': 0.95, 'precision': 0.90, 'recall': 0.85}

        service = ModelTrainingService()
        data = service.load_data()
        model = service.train_model(data)
        evaluation_metrics = service.evaluate_model(model)

        self.assertEqual(model, 'trained_model')
        self.assertEqual(evaluation_metrics['accuracy'], 0.95)
        self.assertEqual(evaluation_metrics['precision'], 0.90)
        self.assertEqual(evaluation_metrics['recall'], 0.85)

    @patch('src.infrastructure.persistence.database_connection.DatabaseConnection.query')
    def test_data_loading(self, query):
        # Simulating the database query to return sample data
        query.return_value = pd.DataFrame({
            'user_id': [1, 2, 3],
            'product_id': [101, 102, 103],
            'rating': [5, 4, 3]
        })

        service = ModelTrainingService()
        result = service.load_data()

        self.assertIsInstance(result, tuple)
        self.assertEqual(len(result), 2)
        self.assertEqual(result[0].shape, (3, 3))

    @patch('src.core.services.model_training_service.ModelTrainingService.preprocess_data')
    def test_data_preprocessing(self, preprocess_data):
        # Simulating the preprocessing to return cleaned data
        preprocess_data.return_value = pd.DataFrame({
            'user_id': [1, 2],
            'product_id': [101, 102],
            'rating': [5, 3]
        })

        service = ModelTrainingService()
        result = service.preprocess_data()

        self.assertIsInstance(result, pd.DataFrame)
        self.assertEqual(result.shape, (2, 3))

    @patch('src.data.feature_engineering.FeatureEngineering.extract_features')
    def test_feature_engineering(self, extract_features):
        # Simulating the feature extraction process
        extract_features.return_value = pd.DataFrame({
            'user_id': [1, 2],
            'product_id': [101, 102],
            'feature_vector': [[0.5, 0.3], [0.2, 0.8]]
        })

        service = ModelTrainingService()
        result = service.feature_engineering()

        self.assertIsInstance(result, pd.DataFrame)
        self.assertEqual(result.shape, (2, 3))

    @patch('src.core.services.model_training_service.ModelTrainingService.split_data')
    def test_data_splitting(self, split_data):
        # Simulating the data splitting process
        split_data.return_value = (pd.DataFrame({
            'user_id': [1],
            'product_id': [101],
            'rating': [5]
        }), pd.DataFrame({
            'user_id': [2],
            'product_id': [102],
            'rating': [3]
        }))

        service = ModelTrainingService()
        train_data, test_data = service.split_data()

        self.assertEqual(train_data.shape, (1, 3))
        self.assertEqual(test_data.shape, (1, 3))

    @patch('src.core.services.model_training_service.ModelTrainingService.train_model')
    def test_model_training(self, train_model):
        # Simulating the training process
        train_model.return_value = 'trained_model'

        service = ModelTrainingService()
        model = service.train_model()

        self.assertEqual(model, 'trained_model')

    @patch('src.core.services.model_training_service.ModelTrainingService.evaluate_model')
    def test_model_evaluation(self, evaluate_model):
        # Simulating the evaluation process
        evaluate_model.return_value = {'accuracy': 0.95, 'precision': 0.90, 'recall': 0.85}

        service = ModelTrainingService()
        metrics = service.evaluate_model('trained_model')

        self.assertEqual(metrics['accuracy'], 0.95)
        self.assertEqual(metrics['precision'], 0.90)
        self.assertEqual(metrics['recall'], 0.85)

    @patch('src.core.services.model_training_service.ModelTrainingService.save_model')
    def test_model_saving(self, save_model):
        # Simulating the model saving process
        save_model.return_value = True

        service = ModelTrainingService()
        result = service.save_model('trained_model')

        self.assertTrue(result)

    @patch('src.core.services.model_training_service.ModelTrainingService.load_model')
    def test_model_loading(self, load_model):
        # Simulating the model loading process
        load_model.return_value = 'loaded_model'

        service = ModelTrainingService()
        model = service.load_model('/model')

        self.assertEqual(model, 'loaded_model')

    @patch('src.core.services.model_training_service.ModelTrainingService.hyperparameter_tuning')
    def test_hyperparameter_tuning(self, hyperparameter_tuning):
        # Simulating the hyperparameter tuning process
        hyperparameter_tuning.return_value = {'best_param': 'param_value'}

        service = ModelTrainingService()
        best_params = service.hyperparameter_tuning()

        self.assertEqual(best_params['best_param'], 'param_value')

if __name__ == '__main__':
    unittest.main()