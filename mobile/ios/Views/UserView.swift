import SwiftUI

struct UserView: View {
    @ObservedObject var viewModel: UserViewModel

    var body: some View {
        VStack {
            if viewModel.isLoading {
                ProgressView("Loading User...")
            } else {
                VStack(spacing: 20) {
                    ProfileImage(image: viewModel.user.profileImage)
                    Text(viewModel.user.name)
                        .font(.largeTitle)
                        .fontWeight(.bold)
                    Text(viewModel.user.email)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                    
                    Divider()
                    
                    UserDetailsView(user: viewModel.user)
                    
                    Spacer()
                    
                    Button(action: {
                        viewModel.logout()
                    }) {
                        Text("Log Out")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(width: 200, height: 50)
                            .background(Color.red)
                            .cornerRadius(10)
                    }
                }
                .padding()
            }
        }
        .onAppear {
            viewModel.loadUser()
        }
    }
}

struct ProfileImage: View {
    var image: UIImage?

    var body: some View {
        if let image = image {
            Image(uiImage: image)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 150, height: 150)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.white, lineWidth: 4))
                .shadow(radius: 10)
        } else {
            Image(systemName: "person.crop.circle.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 150, height: 150)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.white, lineWidth: 4))
                .shadow(radius: 10)
        }
    }
}

struct UserDetailsView: View {
    var user: User

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("Name:")
                    .fontWeight(.bold)
                Text(user.name)
            }

            HStack {
                Text("Email:")
                    .fontWeight(.bold)
                Text(user.email)
            }

            HStack {
                Text("Phone:")
                    .fontWeight(.bold)
                Text(user.phone)
            }

            HStack {
                Text("Address:")
                    .fontWeight(.bold)
                Text(user.address)
            }

            HStack {
                Text("Loyalty Points:")
                    .fontWeight(.bold)
                Text("\(user.loyaltyPoints)")
            }

            HStack {
                Text("Date Joined:")
                    .fontWeight(.bold)
                Text(user.dateJoined, style: .date)
            }
        }
        .padding()
    }
}

class UserViewModel: ObservableObject {
    @Published var user: User
    @Published var isLoading: Bool = false

    init(user: User = User.placeholder) {
        self.user = user
    }

    func loadUser() {
        isLoading = true
        UserService.getUser { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let user):
                    self.user = user
                case .failure(let error):
                    print("Failed to load user: \(error)")
                }
            }
        }
    }

    func logout() {
        AuthService.logout()
    }
}

struct User: Identifiable {
    var id: String
    var name: String
    var email: String
    var phone: String
    var address: String
    var loyaltyPoints: Int
    var dateJoined: Date
    var profileImage: UIImage?

    static let placeholder = User(
        id: "0",
        name: "Person",
        email: "person@website.com",
        phone: "123-456-7890",
        address: "123 Elm St, Springfield, USA",
        loyaltyPoints: 1200,
        dateJoined: Date(),
        profileImage: nil
    )
}

class UserService {
    static func getUser(completion: @escaping (Result<User, Error>) -> Void) {
        // Simulated network request to fetch user data
        DispatchQueue.global().asyncAfter(deadline: .now() + 2) {
            let sampleUser = User(
                id: "123",
                name: "Person",
                email: "person@website.com",
                phone: "123-456-7890",
                address: "123 Elm St, Springfield, USA",
                loyaltyPoints: 1500,
                dateJoined: Date(),
                profileImage: nil
            )
            completion(.success(sampleUser))
        }
    }
}

class AuthService {
    static func logout() {
        // Perform logout functionality
        print("User logged out")
    }
}

struct UserView_Previews: PreviewProvider {
    static var previews: some View {
        UserView(viewModel: UserViewModel())
    }
}