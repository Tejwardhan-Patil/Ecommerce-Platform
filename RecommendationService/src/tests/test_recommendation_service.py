import unittest
from recommendation_service import RecommendationService
from user_profile import UserProfile
from product_profile import ProductProfile
from unittest.mock import patch, MagicMock
import json

class TestRecommendationService(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.recommendation_service = RecommendationService()

    def setUp(self):
        # User and Product profiles to be used in tests
        self.user_profile = UserProfile(user_id=1, name="Person1", preferences=["electronics", "books"])
        self.product_profile = ProductProfile(product_id=101, name="Laptop", category="electronics", price=999.99)

    def test_recommendation_for_existing_user(self):
        with patch('recommendation_service.RecommendationService.get_recommendations') as mocked_get_recommendations:
            mocked_get_recommendations.return_value = [self.product_profile]

            recommendations = self.recommendation_service.get_recommendations(self.user_profile)

            mocked_get_recommendations.assert_called_once_with(self.user_profile)
            self.assertEqual(len(recommendations), 1)
            self.assertEqual(recommendations[0].name, "Laptop")

    def test_recommendation_for_new_user(self):
        new_user_profile = UserProfile(user_id=2, name="Person2", preferences=["fashion", "accessories"])

        with patch('recommendation_service.RecommendationService.get_recommendations') as mocked_get_recommendations:
            mocked_get_recommendations.return_value = []

            recommendations = self.recommendation_service.get_recommendations(new_user_profile)

            mocked_get_recommendations.assert_called_once_with(new_user_profile)
            self.assertEqual(len(recommendations), 0)

    def test_invalid_user_profile(self):
        invalid_user_profile = None

        with self.assertRaises(ValueError):
            self.recommendation_service.get_recommendations(invalid_user_profile)

    def test_recommendations_with_insufficient_data(self):
        incomplete_user_profile = UserProfile(user_id=3, name="Person3", preferences=[])

        with patch('recommendation_service.RecommendationService.get_recommendations') as mocked_get_recommendations:
            mocked_get_recommendations.return_value = []

            recommendations = self.recommendation_service.get_recommendations(incomplete_user_profile)

            mocked_get_recommendations.assert_called_once_with(incomplete_user_profile)
            self.assertEqual(len(recommendations), 0)

    def test_database_connection(self):
        with patch('infrastructure.persistence.database_connection.DatabaseConnection') as mock_db:
            mock_db_instance = mock_db.return_value
            mock_db_instance.connect.return_value = True

            db_conn = mock_db_instance.connect()

            mock_db_instance.connect.assert_called_once()
            self.assertTrue(db_conn)

    def test_recommendation_service_with_database(self):
        with patch('infrastructure.persistence.database_connection.DatabaseConnection') as mock_db, \
             patch('recommendation_service.RecommendationService.get_recommendations') as mocked_get_recommendations:

            mock_db_instance = mock_db.return_value
            mock_db_instance.fetch_user_data.return_value = self.user_profile
            mock_db_instance.fetch_product_data.return_value = [self.product_profile]
            
            mocked_get_recommendations.return_value = [self.product_profile]

            recommendations = self.recommendation_service.get_recommendations(self.user_profile)

            mock_db_instance.fetch_user_data.assert_called_once_with(self.user_profile.user_id)
            mock_db_instance.fetch_product_data.assert_called_once()
            mocked_get_recommendations.assert_called_once_with(self.user_profile)

            self.assertEqual(len(recommendations), 1)

    def test_recommendation_cache(self):
        with patch('recommendation_service.RecommendationService.cache_recommendations') as mock_cache:
            mock_cache.return_value = True

            cache_success = self.recommendation_service.cache_recommendations(self.user_profile, [self.product_profile])

            mock_cache.assert_called_once_with(self.user_profile, [self.product_profile])
            self.assertTrue(cache_success)

    def test_cache_invalidation(self):
        with patch('recommendation_service.RecommendationService.invalidate_cache') as mock_invalidate:
            mock_invalidate.return_value = True

            invalidate_success = self.recommendation_service.invalidate_cache(self.user_profile)

            mock_invalidate.assert_called_once_with(self.user_profile)
            self.assertTrue(invalidate_success)

    def test_model_retraining(self):
        with patch('recommendation_service.model_training_service.ModelTrainingService.train_model') as mock_train_model:
            mock_train_model.return_value = True

            train_success = self.recommendation_service.retrain_model()

            mock_train_model.assert_called_once()
            self.assertTrue(train_success)

    def test_integration_with_http_api(self):
        with patch('infrastructure.http.api') as mock_api:
            mock_response = MagicMock()
            mock_response.json.return_value = {"status": "success", "data": [self.product_profile]}
            mock_api.get.return_value = mock_response

            response = mock_api.get("/recommendations", params={"user_id": self.user_profile.user_id})

            self.assertEqual(response.status_code, 200)
            self.assertEqual(response.json()['status'], "success")
            self.assertEqual(len(response.json()['data']), 1)
            self.assertEqual(response.json()['data'][0]['name'], "Laptop")

    def test_feature_engineering(self):
        with patch('data.feature_engineering.FeatureEngineering') as mock_feature_engineering:
            mock_feature_engineering_instance = mock_feature_engineering.return_value
            mock_feature_engineering_instance.transform.return_value = {"feature_1": 1.0, "feature_2": 0.8}

            transformed_features = mock_feature_engineering_instance.transform(self.user_profile)

            mock_feature_engineering_instance.transform.assert_called_once_with(self.user_profile)
            self.assertEqual(transformed_features["feature_1"], 1.0)

    def test_recommendation_with_preprocessed_data(self):
        with patch('data.data_preprocessing.DataPreprocessing') as mock_data_preprocessing:
            mock_data_preprocessing_instance = mock_data_preprocessing.return_value
            mock_data_preprocessing_instance.clean_data.return_value = True

            cleaned_data_success = mock_data_preprocessing_instance.clean_data(self.user_profile)

            mock_data_preprocessing_instance.clean_data.assert_called_once_with(self.user_profile)
            self.assertTrue(cleaned_data_success)

    def test_model_training_with_custom_params(self):
        with patch('recommendation_service.model_training_service.ModelTrainingService.train_model') as mock_train_model:
            mock_train_model.return_value = True

            custom_params = {
                "epochs": 10,
                "learning_rate": 0.001
            }

            train_success = self.recommendation_service.retrain_model(custom_params)

            mock_train_model.assert_called_once_with(custom_params)
            self.assertTrue(train_success)

    def test_error_handling_in_service(self):
        with patch('recommendation_service.RecommendationService.get_recommendations') as mocked_get_recommendations:
            mocked_get_recommendations.side_effect = Exception("Failed to get recommendations")

            with self.assertRaises(Exception):
                self.recommendation_service.get_recommendations(self.user_profile)

    def test_batch_recommendation_processing(self):
        with patch('recommendation_service.RecommendationService.process_batch_recommendations') as mock_batch:
            mock_batch.return_value = True

            batch_success = self.recommendation_service.process_batch_recommendations([self.user_profile])

            mock_batch.assert_called_once_with([self.user_profile])
            self.assertTrue(batch_success)

if __name__ == '__main__':
    unittest.main()