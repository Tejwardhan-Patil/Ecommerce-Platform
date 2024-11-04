import Foundation
import UIKit

// MARK: - User Model
struct User: Codable {
    let id: String
    let username: String
    let email: String
    let phoneNumber: String
    let address: String
    let profilePictureUrl: String?
    let createdAt: Date
    let updatedAt: Date
}

// MARK: - UserService Protocol
protocol UserServiceProtocol {
    func createUser(user: User, completion: @escaping (Result<User, Error>) -> Void)
    func getUser(byId id: String, completion: @escaping (Result<User, Error>) -> Void)
    func updateUser(user: User, completion: @escaping (Result<User, Error>) -> Void)
    func deleteUser(id: String, completion: @escaping (Result<Bool, Error>) -> Void)
    func fetchAllUsers(completion: @escaping (Result<[User], Error>) -> Void)
}

// MARK: - Network Errors
enum NetworkError: Error {
    case invalidResponse
    case decodingError
    case unknownError
}

// MARK: - UserService Implementation
class UserService: UserServiceProtocol {
    
    // Singleton instance for global access
    static let shared = UserService()
    
    // Private initializer to prevent direct instantiation
    private init() {}
    
    // MARK: - Constants
    private let baseURL = URL(string: "https://api.website.com/users")!
    
    // MARK: - URLSession
    private let session = URLSession.shared
    
    // MARK: - Create User
    func createUser(user: User, completion: @escaping (Result<User, Error>) -> Void) {
        let url = baseURL
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        guard let httpBody = try? JSONEncoder().encode(user) else {
            completion(.failure(NetworkError.unknownError))
            return
        }
        
        request.httpBody = httpBody
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 201 else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            do {
                let createdUser = try JSONDecoder().decode(User.self, from: data)
                completion(.success(createdUser))
            } catch {
                completion(.failure(NetworkError.decodingError))
            }
        }.resume()
    }
    
    // MARK: - Get User by ID
    func getUser(byId id: String, completion: @escaping (Result<User, Error>) -> Void) {
        let url = baseURL.appendingPathComponent(id)
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            do {
                let user = try JSONDecoder().decode(User.self, from: data)
                completion(.success(user))
            } catch {
                completion(.failure(NetworkError.decodingError))
            }
        }.resume()
    }
    
    // MARK: - Update User
    func updateUser(user: User, completion: @escaping (Result<User, Error>) -> Void) {
        let url = baseURL.appendingPathComponent(user.id)
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        guard let httpBody = try? JSONEncoder().encode(user) else {
            completion(.failure(NetworkError.unknownError))
            return
        }
        
        request.httpBody = httpBody
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            do {
                let updatedUser = try JSONDecoder().decode(User.self, from: data)
                completion(.success(updatedUser))
            } catch {
                completion(.failure(NetworkError.decodingError))
            }
        }.resume()
    }
    
    // MARK: - Delete User
    func deleteUser(id: String, completion: @escaping (Result<Bool, Error>) -> Void) {
        let url = baseURL.appendingPathComponent(id)
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 204 else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            completion(.success(true))
        }.resume()
    }
    
    // MARK: - Fetch All Users
    func fetchAllUsers(completion: @escaping (Result<[User], Error>) -> Void) {
        let url = baseURL
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            do {
                let users = try JSONDecoder().decode([User].self, from: data)
                completion(.success(users))
            } catch {
                completion(.failure(NetworkError.decodingError))
            }
        }.resume()
    }
}

// MARK: - Helper Extensions
extension URLSession {
    func dataTask<T: Decodable>(with urlRequest: URLRequest, completion: @escaping (Result<T, Error>) -> Void) {
        self.dataTask(with: urlRequest) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data,
                  let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                completion(.failure(NetworkError.invalidResponse))
                return
            }
            
            do {
                let result = try JSONDecoder().decode(T.self, from: data)
                completion(.success(result))
            } catch {
                completion(.failure(NetworkError.decodingError))
            }
        }.resume()
    }
}

// MARK: - UI/UX Utility for displaying User Profile Pictures
class ImageLoader {
    static let shared = ImageLoader()
    
    private let cache = NSCache<NSString, UIImage>()
    
    func loadImage(from url: URL, completion: @escaping (UIImage?) -> Void) {
        if let cachedImage = cache.object(forKey: url.absoluteString as NSString) {
            completion(cachedImage)
            return
        }
        
        URLSession.shared.dataTask(with: url) { data, response, error in
            guard let data = data, error == nil, let image = UIImage(data: data) else {
                completion(nil)
                return
            }
            
            self.cache.setObject(image, forKey: url.absoluteString as NSString)
            DispatchQueue.main.async {
                completion(image)
            }
        }.resume()
    }
}