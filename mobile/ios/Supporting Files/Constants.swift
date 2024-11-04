import Foundation
import UIKit

// MARK: - API Endpoints
struct API {
    static let baseURL = "https://api.website.com"
    static let productsEndpoint = "\(baseURL)/products"
    static let usersEndpoint = "\(baseURL)/users"
    static let ordersEndpoint = "\(baseURL)/orders"
    static let loginEndpoint = "\(baseURL)/auth/login"
    static let registerEndpoint = "\(baseURL)/auth/register"
    static let paymentEndpoint = "\(baseURL)/payments"
}

// MARK: - HTTP Headers
struct HTTPHeaders {
    static let contentType = "Content-Type"
    static let authorization = "Authorization"
    static let accept = "Accept"
    static let applicationJSON = "application/json"
}

// MARK: - User Defaults Keys
struct UserDefaultsKeys {
    static let accessToken = "accessToken"
    static let refreshToken = "refreshToken"
    static let userID = "userID"
    static let userEmail = "userEmail"
}

// MARK: - Error Messages
struct ErrorMessage {
    static let networkError = "Unable to connect to the server. Please check your internet connection."
    static let decodingError = "Failed to decode the data from the server."
    static let unauthorizedError = "You are not authorized to perform this action."
    static let unknownError = "An unknown error occurred. Please try again later."
}

// MARK: - Success Messages
struct SuccessMessage {
    static let orderPlaced = "Your order has been successfully placed."
    static let profileUpdated = "Your profile has been updated."
    static let paymentSuccessful = "Payment was successful. Thank you!"
}

// MARK: - UI Constants
struct UIConstants {
    static let cornerRadius: CGFloat = 10.0
    static let buttonHeight: CGFloat = 50.0
    static let buttonPadding: CGFloat = 16.0
    static let standardPadding: CGFloat = 8.0
    static let largePadding: CGFloat = 24.0
    static let smallPadding: CGFloat = 4.0
}

// MARK: - Color Constants
struct ColorConstants {
    static let primaryColor = UIColor(red: 0.25, green: 0.47, blue: 0.85, alpha: 1.0)
    static let secondaryColor = UIColor(red: 0.85, green: 0.64, blue: 0.13, alpha: 1.0)
    static let backgroundColor = UIColor.systemBackground
    static let errorColor = UIColor.red
    static let successColor = UIColor.green
}

// MARK: - Fonts
struct FontConstants {
    static let titleFont = UIFont.systemFont(ofSize: 24, weight: .bold)
    static let subtitleFont = UIFont.systemFont(ofSize: 18, weight: .medium)
    static let bodyFont = UIFont.systemFont(ofSize: 16, weight: .regular)
    static let smallFont = UIFont.systemFont(ofSize: 12, weight: .light)
}

// MARK: - API Timeout
struct APITimeout {
    static let requestTimeout: TimeInterval = 30.0
    static let resourceTimeout: TimeInterval = 60.0
}

// MARK: - Notification Constants
struct NotificationConstants {
    static let userLoggedIn = Notification.Name("userLoggedIn")
    static let userLoggedOut = Notification.Name("userLoggedOut")
    static let orderPlaced = Notification.Name("orderPlaced")
}

// MARK: - Accessibility Identifiers
struct AccessibilityIdentifiers {
    static let loginButton = "login_button"
    static let registerButton = "register_button"
    static let profileImageView = "profile_image_view"
    static let productCell = "product_cell"
    static let orderButton = "order_button"
}

// MARK: - Animation Durations
struct AnimationDuration {
    static let short: TimeInterval = 0.3
    static let medium: TimeInterval = 0.6
    static let long: TimeInterval = 1.0
}

// MARK: - API Status Codes
struct HTTPStatusCode {
    static let success = 200
    static let badRequest = 400
    static let unauthorized = 401
    static let forbidden = 403
    static let notFound = 404
    static let internalServerError = 500
}

// MARK: - Date Formats
struct DateFormats {
    static let apiDateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
    static let displayDateFormat = "MMMM d, yyyy"
    static let timeFormat = "HH:mm"
}

// MARK: - App URLs
struct AppURLs {
    static let termsOfService = "https://website.com/terms"
    static let privacyPolicy = "https://website.com/privacy"
    static let helpCenter = "https://website.com/help"
}

// MARK: - App Settings
struct AppSettings {
    static let isLoggingEnabled = true
    static let isAnalyticsEnabled = true
    static let maximumRetryCount = 3
}

// MARK: - Validation Messages
struct ValidationMessage {
    static let emailInvalid = "Please enter a valid email address."
    static let passwordTooShort = "Password must be at least 8 characters."
    static let nameRequired = "Name is required."
}

// MARK: - Feature Flags
struct FeatureFlags {
    static let enableNewCheckoutFlow = true
    static let enableProductRecommendations = true
    static let enableDarkModeSupport = true
}

// MARK: - Notification Center Keys
struct NotificationKeys {
    static let didReceiveNotification = "didReceiveNotification"
    static let didUpdateCart = "didUpdateCart"
    static let didUpdateProfile = "didUpdateProfile"
}

// MARK: - Analytics Event Names
struct AnalyticsEventNames {
    static let userLoggedIn = "UserLoggedIn"
    static let userRegistered = "UserRegistered"
    static let orderPlaced = "OrderPlaced"
    static let paymentCompleted = "PaymentCompleted"
}

// MARK: - App Limits
struct AppLimits {
    static let maxProductNameLength = 50
    static let maxPasswordLength = 16
    static let minPasswordLength = 8
}

// MARK: - Localization Keys
struct LocalizationKeys {
    static let welcomeMessage = "welcome_message"
    static let loginTitle = "login_title"
    static let registerTitle = "register_title"
    static let productTitle = "product_title"
    static let orderSummary = "order_summary"
}

// MARK: - Environment Variables
struct Environment {
    static let apiKey = "API_KEY"
    static let environment = "PRODUCTION"
}

// MARK: - Device Types
struct DeviceType {
    static let isIpad = UIDevice.current.userInterfaceIdiom == .pad
    static let isIphone = UIDevice.current.userInterfaceIdiom == .phone
}

// MARK: - Logging Constants
struct Logging {
    static let networkTag = "NETWORK"
    static let databaseTag = "DATABASE"
    static let uiTag = "UI"
    static let errorTag = "ERROR"
}