import SwiftUI

struct ProductView: View {
    var product: Product

    @State private var selectedImageIndex = 0
    @State private var quantity: Int = 1
    @EnvironmentObject var cart: CartViewModel

    var body: some View {
        VStack {
            // Product Image Carousel
            TabView(selection: $selectedImageIndex) {
                ForEach(0..<product.images.count, id: \.self) { index in
                    Image(uiImage: UIImage(named: product.images[index])!)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .tag(index)
                }
            }
            .frame(height: 300)
            .tabViewStyle(PageTabViewStyle())

            // Product Info
            VStack(alignment: .leading, spacing: 8) {
                Text(product.name)
                    .font(.title)
                    .fontWeight(.bold)

                Text("$\(String(format: "%.2f", product.price))")
                    .font(.title2)
                    .foregroundColor(.gray)

                Text(product.description)
                    .font(.body)
                    .lineLimit(5)
                    .padding(.top, 5)

                HStack {
                    Text("Quantity:")
                    Stepper(value: $quantity, in: 1...10) {
                        Text("\(quantity)")
                    }
                }
                .padding(.top, 10)

                // Add to Cart Button
                Button(action: {
                    cart.addToCart(product: product, quantity: quantity)
                }) {
                    Text("Add to Cart")
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.blue)
                        .cornerRadius(10)
                }
                .padding(.top, 20)
            }
            .padding()
            
            Spacer()
        }
        .navigationBarTitle(product.name, displayMode: .inline)
    }
}

struct Product: Identifiable {
    var id: UUID
    var name: String
    var description: String
    var price: Double
    var images: [String]
}

class CartViewModel: ObservableObject {
    @Published var items: [CartItem] = []

    func addToCart(product: Product, quantity: Int) {
        let item = CartItem(product: product, quantity: quantity)
        items.append(item)
    }
}

struct CartItem: Identifiable {
    var id = UUID()
    var product: Product
    var quantity: Int
}

struct ProductView_Previews: PreviewProvider {
    static var previews: some View {
        let product = Product(id: UUID(), name: "Test Product", description: "This is a test description.", price: 99.99, images: ["image1", "image2"])
        ProductView(product: product)
            .environmentObject(CartViewModel())
    }
}