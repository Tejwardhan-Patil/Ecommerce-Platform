import Foundation

enum UserRole: String, Codable {
    case admin = "Admin"
    case customer = "Customer"
    case guest = "Guest"
}

enum AccountStatus: String, Codable {
    case active = "Active"
    case inactive = "Inactive"
    case suspended = "Suspended"
    case closed = "Closed"
}

struct Address: Codable {
    var street: String
    var city: String
    var state: String
    var zipCode: String
    var country: String
    
    init(street: String, city: String, state: String, zipCode: String, country: String) {
        self.street = street
        self.city = city
        self.state = state
        self.zipCode = zipCode
        self.country = country
    }
}

struct UserPreferences: Codable {
    var language: String
    var currency: String
    var marketingOptIn: Bool
    
    init(language: String = "en", currency: String = "USD", marketingOptIn: Bool = true) {
        self.language = language
        self.currency = currency
        self.marketingOptIn = marketingOptIn
    }
}

struct UserSession: Codable {
    var sessionToken: String
    var lastLoginDate: Date
    var expiresAt: Date
    
    init(sessionToken: String, lastLoginDate: Date, expiresAt: Date) {
        self.sessionToken = sessionToken
        self.lastLoginDate = lastLoginDate
        self.expiresAt = expiresAt
    }
}

struct PaymentMethod: Codable {
    var cardType: String
    var cardNumber: String
    var expiryDate: String
    var cvv: String
    var billingAddress: Address
    
    init(cardType: String, cardNumber: String, expiryDate: String, cvv: String, billingAddress: Address) {
        self.cardType = cardType
        self.cardNumber = cardNumber
        self.expiryDate = expiryDate
        self.cvv = cvv
        self.billingAddress = billingAddress
    }
}

struct OrderHistory: Codable {
    var orderId: String
    var orderDate: Date
    var totalAmount: Double
    
    init(orderId: String, orderDate: Date, totalAmount: Double) {
        self.orderId = orderId
        self.orderDate = orderDate
        self.totalAmount = totalAmount
    }
}

struct User: Codable {
    var userId: String
    var firstName: String
    var lastName: String
    var email: String
    var passwordHash: String
    var role: UserRole
    var accountStatus: AccountStatus
    var shippingAddress: Address
    var billingAddress: Address
    var preferences: UserPreferences
    var session: UserSession?
    var paymentMethods: [PaymentMethod]
    var orderHistory: [OrderHistory]
    
    init(userId: String, firstName: String, lastName: String, email: String, passwordHash: String, role: UserRole, accountStatus: AccountStatus, shippingAddress: Address, billingAddress: Address, preferences: UserPreferences, session: UserSession?, paymentMethods: [PaymentMethod], orderHistory: [OrderHistory]) {
        self.userId = userId
        self.firstName = firstName
        self.lastName = lastName
        self.email = email
        self.passwordHash = passwordHash
        self.role = role
        self.accountStatus = accountStatus
        self.shippingAddress = shippingAddress
        self.billingAddress = billingAddress
        self.preferences = preferences
        self.session = session
        self.paymentMethods = paymentMethods
        self.orderHistory = orderHistory
    }
    
    func fullName() -> String {
        return "\(firstName) \(lastName)"
    }
    
    func updateEmail(newEmail: String) -> User {
        var updatedUser = self
        updatedUser.email = newEmail
        return updatedUser
    }
    
    func addPaymentMethod(newPaymentMethod: PaymentMethod) -> User {
        var updatedUser = self
        updatedUser.paymentMethods.append(newPaymentMethod)
        return updatedUser
    }
    
    func addOrderHistory(newOrder: OrderHistory) -> User {
        var updatedUser = self
        updatedUser.orderHistory.append(newOrder)
        return updatedUser
    }
    
    func isActive() -> Bool {
        return accountStatus == .active
    }
    
    mutating func deactivateAccount() {
        accountStatus = .inactive
    }
    
    mutating func suspendAccount() {
        accountStatus = .suspended
    }
    
    mutating func closeAccount() {
        accountStatus = .closed
    }
}

extension User {
    static func sampleUser() -> User {
        let shippingAddress = Address(street: "123 Apple St", city: "Cupertino", state: "CA", zipCode: "95014", country: "USA")
        let billingAddress = Address(street: "456 Orange Ave", city: "San Francisco", state: "CA", zipCode: "94107", country: "USA")
        let preferences = UserPreferences(language: "en", currency: "USD")
        let session = UserSession(sessionToken: "xyz123", lastLoginDate: Date(), expiresAt: Date().addingTimeInterval(3600 * 24))
        let paymentMethod = PaymentMethod(cardType: "Visa", cardNumber: "4111111111111111", expiryDate: "12/25", cvv: "123", billingAddress: billingAddress)
        let orderHistory = OrderHistory(orderId: "ORD123", orderDate: Date(), totalAmount: 199.99)
        
        return User(userId: "USR123", firstName: "Person", lastName: "Person2", email: "person@website.com", passwordHash: "hashedpassword", role: .customer, accountStatus: .active, shippingAddress: shippingAddress, billingAddress: billingAddress, preferences: preferences, session: session, paymentMethods: [paymentMethod], orderHistory: [orderHistory])
    }
    
    static func adminUser() -> User {
        let address = Address(street: "789 Peach Ln", city: "Los Angeles", state: "CA", zipCode: "90001", country: "USA")
        let preferences = UserPreferences(language: "en", currency: "USD")
        let session = UserSession(sessionToken: "admin123", lastLoginDate: Date(), expiresAt: Date().addingTimeInterval(3600 * 24 * 7))
        return User(userId: "USR456", firstName: "Admin", lastName: "Person", email: "admin@website.com", passwordHash: "hashedadminpassword", role: .admin, accountStatus: .active, shippingAddress: address, billingAddress: address, preferences: preferences, session: session, paymentMethods: [], orderHistory: [])
    }
}