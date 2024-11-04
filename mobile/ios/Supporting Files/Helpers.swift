import Foundation
import UIKit

// MARK: - Helpers for UI Customization

// Extension to easily create a UIColor from hex values
extension UIColor {
    convenience init(hex: String) {
        var cString: String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        if cString.hasPrefix("#") {
            cString.remove(at: cString.startIndex)
        }
        
        if cString.count != 6 {
            self.init(white: 0.5, alpha: 1.0)
            return
        }
        
        var rgbValue: UInt64 = 0
        Scanner(string: cString).scanHexInt64(&rgbValue)
        
        self.init(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
}

// Extension for UILabel to apply common styles
extension UILabel {
    func applyTitleStyle() {
        self.textColor = UIColor(hex: "#333333")
        self.font = UIFont.boldSystemFont(ofSize: 24)
    }
    
    func applyBodyStyle() {
        self.textColor = UIColor(hex: "#666666")
        self.font = UIFont.systemFont(ofSize: 16)
    }
}

// MARK: - Helpers for String Manipulation

// Helper to check if string contains only numeric characters
extension String {
    func isNumeric() -> Bool {
        return !self.isEmpty && self.range(of: "[^0-9]", options: .regularExpression) == nil
    }
    
    // Helper to format string as currency
    func toCurrencyFormat() -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencySymbol = "$"
        let number = NSNumber(value: Double(self) ?? 0.0)
        return formatter.string(from: number) ?? "$0.00"
    }
}

// Helper to sanitize input string by removing extra spaces
extension String {
    func trimWhiteSpaces() -> String {
        return self.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - Helpers for Date Manipulation

// Extension for Date to format into a string
extension Date {
    func toString(format: String = "yyyy-MM-dd") -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        return dateFormatter.string(from: self)
    }
    
    // Helper to calculate days between two dates
    func daysBetween(_ toDate: Date) -> Int {
        return Calendar.current.dateComponents([.day], from: self, to: toDate).day ?? 0
    }
}

// Helper to convert string to Date
extension String {
    func toDate(format: String = "yyyy-MM-dd") -> Date? {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        return dateFormatter.date(from: self)
    }
}

// MARK: - Helpers for Device and System Info

// Helper to get the device model
func getDeviceModel() -> String {
    var systemInfo = utsname()
    uname(&systemInfo)
    let machineMirror = Mirror(reflecting: systemInfo.machine)
    let identifier = machineMirror.children.compactMap { $0.value as? Int8 }.filter { $0 != 0 }.map { String(UnicodeScalar(UInt8($0))) }.joined()
    return identifier
}

// Helper to check if the device is iPhone X or newer
func isIPhoneXOrNewer() -> Bool {
    let deviceModel = getDeviceModel()
    return deviceModel.contains("iPhone10") || deviceModel.contains("iPhone11") || deviceModel.contains("iPhone12") || deviceModel.contains("iPhone13")
}

// Helper to get iOS version
func getiOSVersion() -> String {
    return UIDevice.current.systemVersion
}

// MARK: - Helpers for Alerts

// Helper to display a basic alert
func showAlert(title: String, message: String, on viewController: UIViewController) {
    let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
    viewController.present(alert, animated: true, completion: nil)
}

// Helper to display an action sheet
func showActionSheet(title: String, message: String?, actions: [UIAlertAction], on viewController: UIViewController) {
    let actionSheet = UIAlertController(title: title, message: message, preferredStyle: .actionSheet)
    for action in actions {
        actionSheet.addAction(action)
    }
    viewController.present(actionSheet, animated: true, completion: nil)
}

// MARK: - Helpers for File Handling

// Helper to save data to a file
func saveDataToFile(data: Data, fileName: String) -> Bool {
    let fileManager = FileManager.default
    guard let documentDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else { return false }
    let fileURL = documentDirectory.appendingPathComponent(fileName)
    
    do {
        try data.write(to: fileURL)
        return true
    } catch {
        print("Error saving file: \(error)")
        return false
    }
}

// Helper to load data from a file
func loadDataFromFile(fileName: String) -> Data? {
    let fileManager = FileManager.default
    guard let documentDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else { return nil }
    let fileURL = documentDirectory.appendingPathComponent(fileName)
    
    do {
        let data = try Data(contentsOf: fileURL)
        return data
    } catch {
        print("Error loading file: \(error)")
        return nil
    }
}

// MARK: - Helpers for Networking

// Helper to make a simple GET request
func makeGetRequest(urlString: String, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
    guard let url = URL(string: urlString) else { return }
    
    let task = URLSession.shared.dataTask(with: url) { data, response, error in
        completion(data, response, error)
    }
    task.resume()
}

// Helper to make a POST request with JSON body
func makePostRequest(urlString: String, jsonData: Data, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
    guard let url = URL(string: urlString) else { return }
    
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    
    let task = URLSession.shared.uploadTask(with: request, from: jsonData) { data, response, error in
        completion(data, response, error)
    }
    task.resume()
}

// MARK: - Helpers for Data Encryption

// Helper to encrypt data using AES256
func encryptDataAES256(data: Data, key: Data) -> Data? {
    let keyLength = kCCKeySizeAES256
    var numBytesEncrypted: size_t = 0
    let dataOut = UnsafeMutableRawPointer.allocate(byteCount: data.count + keyLength, alignment: 1)
    let cryptStatus = CCCrypt(
        CCOperation(kCCEncrypt), 
        CCAlgorithm(kCCAlgorithmAES), 
        CCOptions(kCCOptionPKCS7Padding), 
        key.bytes, keyLength, nil, 
        data.bytes, data.count, 
        dataOut, data.count + keyLength, 
        &numBytesEncrypted
    )
    
    if cryptStatus == kCCSuccess {
        return Data(bytes: dataOut, count: numBytesEncrypted)
    } else {
        print("Error encrypting data")
        return nil
    }
}

// Helper to decrypt data using AES256
func decryptDataAES256(data: Data, key: Data) -> Data? {
    let keyLength = kCCKeySizeAES256
    var numBytesDecrypted: size_t = 0
    let dataOut = UnsafeMutableRawPointer.allocate(byteCount: data.count + keyLength, alignment: 1)
    let cryptStatus = CCCrypt(
        CCOperation(kCCDecrypt), 
        CCAlgorithm(kCCAlgorithmAES), 
        CCOptions(kCCOptionPKCS7Padding), 
        key.bytes, keyLength, nil, 
        data.bytes, data.count, 
        dataOut, data.count + keyLength, 
        &numBytesDecrypted
    )
    
    if cryptStatus == kCCSuccess {
        return Data(bytes: dataOut, count: numBytesDecrypted)
    } else {
        print("Error decrypting data")
        return nil
    }
}