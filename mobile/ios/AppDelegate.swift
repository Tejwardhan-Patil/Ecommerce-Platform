import UIKit
import UserNotifications

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    // MARK: - App Launch
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Initialize window
        window = UIWindow(frame: UIScreen.main.bounds)
        let rootViewController = ViewController()
        window?.rootViewController = rootViewController
        window?.makeKeyAndVisible()
        
        // Configure push notifications
        configurePushNotifications(application)

        // Configure background fetch
        application.setMinimumBackgroundFetchInterval(UIApplication.backgroundFetchIntervalMinimum)

        // Handle deep linking
        if let url = launchOptions?[.url] as? URL {
            handleIncomingURL(url)
        }
        
        return true
    }

    // MARK: - Background Fetch
    func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // Perform background data fetch
        fetchDataInBackground { success in
            if success {
                completionHandler(.newData)
            } else {
                completionHandler(.failed)
            }
        }
    }

    // MARK: - Universal Links
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb, let url = userActivity.webpageURL {
            handleIncomingURL(url)
        }
        return true
    }

    // MARK: - Handling Deep Links
    private func handleIncomingURL(_ url: URL) {
        // Process the incoming URL (Universal Link or custom URL scheme)
        let urlString = url.absoluteString
        // Handling of different URLs
        if urlString.contains("product") {
            navigateToProductPage(with: url)
        } else if urlString.contains("profile") {
            navigateToUserProfile(with: url)
        }
    }

    private func navigateToProductPage(with url: URL) {
        // Logic to navigate to product page using the URL
    }

    private func navigateToUserProfile(with url: URL) {
        // Logic to navigate to user profile using the URL
    }

    // MARK: - Push Notifications
    private func configurePushNotifications(_ application: UIApplication) {
        let center = UNUserNotificationCenter.current()
        center.delegate = self

        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Send device token to the server
        let token = deviceToken.reduce("", { $0 + String(format: "%02x", $1) })
        print("Device Token: \(token)")
        // Register device token with backend service
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // Handle failure of push notification registration
        print("Failed to register for remote notifications: \(error)")
    }

    // MARK: - App State Transitions
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Called when the app moves to the background
        saveAppState()
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state
        restoreAppState()
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks paused when the app was inactive
        resetAppBadgeCount()
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Save data if appropriate before the app terminates
        saveAppState()
    }

    // MARK: - App State Management
    private func saveAppState() {
        // Save the current app state, user session, or any in-progress data
    }

    private func restoreAppState() {
        // Restore app state or user session upon relaunch
    }

    private func resetAppBadgeCount() {
        // Reset app's notification badge count
        UIApplication.shared.applicationIconBadgeNumber = 0
    }

    // MARK: - Background Data Fetching
    private func fetchDataInBackground(completion: @escaping (Bool) -> Void) {
        // Simulate background data fetching
        DispatchQueue.global().asyncAfter(deadline: .now() + 1) {
            completion(true) // Call completion with success or failure
        }
    }

    // MARK: - Remote Notification Handling
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // Handle the push notification data
        if let message = userInfo["message"] as? String {
            print("Received Push Notification: \(message)")
        }

        // Perform necessary tasks, then call completion
        completionHandler(.newData)
    }
}

// MARK: - UNUserNotificationCenterDelegate
extension AppDelegate: UNUserNotificationCenterDelegate {

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        // Handle push notifications when app is in foreground
        completionHandler([.alert, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        // Handle user interaction with push notifications
        let userInfo = response.notification.request.content.userInfo
        if let message = userInfo["message"] as? String {
            print("User interacted with notification: \(message)")
        }

        // Call completion
        completionHandler()
    }
}