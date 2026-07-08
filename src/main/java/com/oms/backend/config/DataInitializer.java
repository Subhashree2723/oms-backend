package com.oms.backend.config;

import com.oms.backend.entity.*;
import com.oms.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds roles, a default admin user, and a full category -> subcategory ->
 * product tree on first startup so the storefront isn't empty.
 *
 * Images are real photographs pulled from loremflickr.com, matched to
 * each item by keyword tags derived from its name, and locked to a hash
 * of the name so the same category/product always gets the same picture
 * across restarts.
 *
 * This is meant as a placeholder set, not final product photography — use
 * the "Categories" / "Products" screens in the Admin panel to upload real
 * photos for anything you want to replace; the upload feature there
 * already works and will override these placeholders.
 *
 * Default admin credentials: username=admin / password=Admin@123
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------------
    // Placeholder photo generator — real, keyword-matched photographs.
    //
    // Uses LoremFlickr (loremflickr.com), which serves real Flickr photos
    // filtered by keyword tags extracted from the item's name (e.g.
    // "Wireless Bluetooth Earbuds" -> tags "wireless,bluetooth,earbuds"),
    // so the placeholder actually looks like the thing it's labelling
    // instead of a random unrelated photo. The `lock` value is a hash of
    // the name, so the same category/product always gets the same photo
    // across restarts instead of a new random one every reload.
    //
    // These are plain https:// URLs, so the Angular ImageUrlPipe passes
    // them straight through. Replace via the Admin "Categories"/"Products"
    // upload feature for real product photography.
    // ---------------------------------------------------------------

    private static final List<String> STOPWORDS = List.of(
            "and", "with", "for", "the", "of", "a", "an", "set", "pack", "combo", "kit", "size");

    private static String tagsFor(String label) {
        String cleaned = label.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String w : cleaned.split("\\s+")) {
            if (w.isBlank() || STOPWORDS.contains(w)) continue;
            if (count > 0) sb.append(",");
            sb.append(w);
            count++;
            if (count >= 3) break; // keep the query focused on the most relevant words
        }
        return sb.length() == 0 ? cleaned.replace(" ", "") : sb.toString();
    }

    private static long lockFor(String label) {
        return Math.abs((long) label.hashCode());
    }

    private static String catImage(String label) {
        return "https://loremflickr.com/300/300/" + tagsFor(label) + "/all?lock=" + lockFor(label);
    }

    private static String productImage(String label) {
        return "https://loremflickr.com/400/400/" + tagsFor(label) + "/all?lock=" + lockFor(label);
    }

    // ---------------------------------------------------------------
    // Seed data model
    // ---------------------------------------------------------------

    private record SubCat(String name, String emoji) {
    }

    private record SeedProduct(String name, String emoji, String description, String price, String gstPercent, int stock) {
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CUSTOMER").build()));

        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@oms.com")
                    .role(adminRole)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }

        if (categoryRepository.count() == 0) {
            int p = 0;

            seedTopCategory("Mobiles", "\uD83D\uDCF1", p++, "Smartphones and mobile accessories",
                    Arrays.asList(
                            new SubCat("iPhone", "\uD83D\uDCF1"), new SubCat("Samsung", "\uD83D\uDCF1"),
                            new SubCat("OnePlus", "\uD83D\uDCF1"), new SubCat("Redmi", "\uD83D\uDCF1"),
                            new SubCat("realme", "\uD83D\uDCF1"), new SubCat("Vivo", "\uD83D\uDCF1"),
                            new SubCat("OPPO", "\uD83D\uDCF1"), new SubCat("Google Pixel", "\uD83D\uDCF1"),
                            new SubCat("Mobile Cases & Covers", "\uD83D\uDEE1"), new SubCat("Chargers & Cables", "\uD83D\uDD0C")),
                    new SeedProduct("Galaxy Smart Phone X12", "\uD83D\uDCF1", "6.5\" AMOLED, 128GB storage", "499.00", "12", 30),
                    new SeedProduct("Budget Smartphone A5", "\uD83D\uDCF1", "5000mAh battery, dual camera", "179.00", "12", 60),
                    new SeedProduct("Phone Case & Screen Guard Combo", "\uD83D\uDEE1", "Shockproof case with tempered glass", "9.99", "18", 200));

            seedTopCategory("Fashion", "\uD83D\uDC57", p++, "Clothing, footwear and accessories",
                    Arrays.asList(
                            new SubCat("Men's Shirts & Tees", "\uD83D\uDC55"), new SubCat("Women's Kurtas & Sets", "\uD83E\uDDF5"),
                            new SubCat("Jeans", "\uD83D\uDC56"), new SubCat("Sarees", "\uD83E\uDDF5"),
                            new SubCat("Sports Shoes", "\uD83D\uDC5F"), new SubCat("Watches", "\u231A"),
                            new SubCat("Jewellery", "\uD83D\uDC8D"), new SubCat("Bags & Luggage", "\uD83D\uDC5C"),
                            new SubCat("Kids' Clothing", "\uD83E\uDDE6"), new SubCat("Winterwear", "\uD83E\uDDE3")),
                    new SeedProduct("Men's Casual Shirt", "\uD83D\uDC55", "100% cotton, slim fit", "19.99", "5", 80),
                    new SeedProduct("Women's Running Shoes", "\uD83D\uDC5F", "Lightweight, breathable mesh", "34.99", "5", 45),
                    new SeedProduct("Unisex Backpack", "\uD83C\uDF92", "Water-resistant, laptop compartment", "24.99", "5", 70));

            seedTopCategory("Electronics", "\uD83D\uDCBB", p++, "Laptops, cameras and gadgets",
                    Arrays.asList(
                            new SubCat("Laptops", "\uD83D\uDCBB"), new SubCat("Tablets", "\uD83D\uDCF2"),
                            new SubCat("Smart Watches", "\u231A"), new SubCat("Headphones & Earbuds", "\uD83C\uDFA7"),
                            new SubCat("Cameras", "\uD83D\uDCF7"), new SubCat("Smart TVs", "\uD83D\uDCFA"),
                            new SubCat("Power Banks", "\uD83D\uDD0B"), new SubCat("Routers & Networking", "\uD83D\uDCE1"),
                            new SubCat("Gaming Consoles", "\uD83C\uDFAE"), new SubCat("Printers", "\uD83D\uDDA8")),
                    new SeedProduct("Wireless Bluetooth Earbuds", "\uD83C\uDFA7", "Noise-cancelling, 24h battery life", "199.00", "18", 50),
                    new SeedProduct("Smart LED TV 43\"", "\uD83D\uDCFA", "4K UHD, Android smart TV", "349.00", "18", 20),
                    new SeedProduct("Portable Power Bank 20000mAh", "\uD83D\uDD0B", "Fast charging, dual USB", "29.99", "18", 100));

            seedTopCategory("Beauty & Personal Care", "\uD83D\uDC84", p++, "Skincare, haircare and cosmetics",
                    Arrays.asList(
                            new SubCat("Skin Care", "\u2728"), new SubCat("Hair Care", "\uD83D\uDC87"),
                            new SubCat("Makeup", "\uD83D\uDC84"), new SubCat("Fragrances", "\uD83C\uDF38"),
                            new SubCat("Men's Grooming", "\uD83E\uDEAE"), new SubCat("Oral Care", "\uD83E\uDEA5"),
                            new SubCat("Personal Hygiene", "\uD83E\uDDFC"), new SubCat("Beauty Combos", "\uD83C\uDF81")),
                    new SeedProduct("Herbal Face Wash", "\u2728", "For all skin types, 100ml", "6.99", "18", 150),
                    new SeedProduct("Hair Care Combo Pack", "\uD83D\uDC87", "Shampoo + conditioner set", "12.99", "18", 90),
                    new SeedProduct("Perfume Spray 100ml", "\uD83C\uDF38", "Long-lasting fragrance", "22.99", "18", 60));

            seedTopCategory("Home & Furniture", "\uD83E\uDE91", p++, "Furniture and home decor",
                    Arrays.asList(
                            new SubCat("Sofas & Recliners", "\uD83D\uDECB"), new SubCat("Beds & Wardrobes", "\uD83D\uDECF"),
                            new SubCat("Dining Sets", "\uD83C\uDF7D"), new SubCat("Home Decor", "\uD83D\uDD6F"),
                            new SubCat("Lighting", "\uD83D\uDCA1"), new SubCat("Bedsheets & Curtains", "\uD83E\uDDF5"),
                            new SubCat("Storage & Organizers", "\uD83D\uDCE6")),
                    new SeedProduct("3-Seater Fabric Sofa", "\uD83D\uDECB", "Modern design, stain-resistant fabric", "349.00", "12", 15),
                    new SeedProduct("Study Table with Storage", "\uD83E\uDE91", "Engineered wood, 2 drawers", "89.99", "12", 25));

            seedTopCategory("Home Appliances", "\uD83E\uDDFA", p++, "Kitchen and home appliances",
                    Arrays.asList(
                            new SubCat("Refrigerators", "\u2744"), new SubCat("Washing Machines", "\uD83E\uDDFA"),
                            new SubCat("Air Conditioners", "\u2744"), new SubCat("Kitchen Appliances", "\uD83C\uDF73"),
                            new SubCat("Mixers & Grinders", "\uD83E\uDD64"), new SubCat("Air & Water Purifiers", "\uD83D\uDCA7"),
                            new SubCat("Fans & Coolers", "\uD83D\uDF00")),
                    new SeedProduct("Mixer Grinder 750W", "\uD83E\uDD64", "3 jars, stainless steel blades", "39.99", "18", 40),
                    new SeedProduct("Electric Kettle 1.5L", "\u2615", "Auto shut-off, fast boil", "14.99", "18", 90),
                    new SeedProduct("Air Fryer 4L", "\uD83C\uDF57", "Oil-free frying, digital display", "59.99", "18", 25));

            seedTopCategory("Toys, Baby & Kids", "\uD83E\uDDF8", p++, "Toys, baby care and kids' essentials",
                    Arrays.asList(
                            new SubCat("Soft Toys", "\uD83E\uDDF8"), new SubCat("Building Blocks", "\uD83E\uDDF1"),
                            new SubCat("Baby Care", "\uD83C\uDF7C"), new SubCat("Diapers & Wipes", "\uD83D\uDC76"),
                            new SubCat("Educational Toys", "\uD83E\uDDE9"), new SubCat("Remote Control Toys", "\uD83D\uDE97"),
                            new SubCat("Strollers & Car Seats", "\uD83D\uDE7C")),
                    new SeedProduct("Plush Teddy Bear 24\"", "\uD83E\uDDF8", "Soft and huggable, machine washable", "17.99", "12", 60),
                    new SeedProduct("Building Blocks Set (300 pcs)", "\uD83E\uDDF1", "Educational STEM toy for ages 3+", "24.99", "12", 45));

            seedTopCategory("Food & Health / Grocery", "\uD83D\uDED2", p++, "Daily grocery and health essentials",
                    Arrays.asList(
                            new SubCat("Staples & Rice", "\uD83C\uDF5A"), new SubCat("Cooking Oil & Ghee", "\uD83E\uDED9"),
                            new SubCat("Snacks & Beverages", "\uD83C\uDF6A"), new SubCat("Health Supplements", "\uD83D\uDC8A"),
                            new SubCat("Dairy & Bakery", "\uD83E\uDD5B"), new SubCat("Organic & Health Foods", "\uD83E\uDD57")),
                    new SeedProduct("Basmati Rice 5kg", "\uD83C\uDF5A", "Premium long-grain rice", "11.99", "5", 120),
                    new SeedProduct("Cooking Oil 1L", "\uD83E\uDED9", "Refined sunflower oil", "4.99", "5", 150),
                    new SeedProduct("Assorted Snacks Pack", "\uD83C\uDF6A", "Pack of 6 mixed snacks", "7.99", "12", 100));

            seedTopCategory("Auto Accessories", "\uD83D\uDE97", p++, "Car care and accessories",
                    Arrays.asList(
                            new SubCat("Car Interior Accessories", "\uD83D\uDE97"), new SubCat("Car Care & Cleaning", "\uD83E\uDDFD"),
                            new SubCat("Car Electronics", "\uD83D\uDCFB"), new SubCat("Tyres & Wheel Care", "\u2699"),
                            new SubCat("Helmets & Riding Gear", "\uD83E\uDE96")),
                    new SeedProduct("Car Seat Cover Set", "\uD83D\uDE97", "Universal fit, premium leatherette", "44.99", "18", 35),
                    new SeedProduct("Microfiber Car Cleaning Kit", "\uD83E\uDDFD", "Wash mitt, cloths and cleaner", "19.99", "18", 55));

            seedTopCategory("2 Wheelers", "\uD83C\uDFCD", p++, "Bikes, scooters and riding gear",
                    Arrays.asList(
                            new SubCat("Motorcycle Accessories", "\uD83C\uDFCD"), new SubCat("Scooter Accessories", "\uD83D\uDEF5"),
                            new SubCat("Riding Helmets", "\uD83E\uDE96"), new SubCat("Bike Care Products", "\uD83E\uDDFD"),
                            new SubCat("Riding Jackets & Gloves", "\uD83E\uDDE4")),
                    new SeedProduct("Full-Face Riding Helmet", "\uD83E\uDE96", "ISI certified, anti-fog visor", "54.99", "18", 40),
                    new SeedProduct("Bike Cover (Water Resistant)", "\uD83C\uDFCD", "UV & dust protection, universal size", "16.99", "18", 65));

            seedTopCategory("Sports & Fitness", "\u26F9", p++, "Sports gear and fitness equipment",
                    Arrays.asList(
                            new SubCat("Fitness Equipment", "\uD83C\uDFCB"), new SubCat("Cricket Gear", "\uD83C\uDFCF"),
                            new SubCat("Cycling", "\uD83D\uDEB4"), new SubCat("Yoga & Wellness", "\uD83E\uDDD8"),
                            new SubCat("Sports Shoes & Apparel", "\uD83D\uDC5F"), new SubCat("Outdoor & Camping", "\u26FA")),
                    new SeedProduct("Adjustable Dumbbell Set 20kg", "\uD83C\uDFCB", "Pair with adjustable weight plates", "64.99", "18", 30),
                    new SeedProduct("Yoga Mat with Carry Strap", "\uD83E\uDDD8", "6mm anti-slip, extra cushioning", "15.99", "12", 90));

            seedTopCategory("Books & Stationery", "\uD83D\uDCDA", p++, "Books, stationery and office supplies",
                    Arrays.asList(
                            new SubCat("Fiction & Novels", "\uD83D\uDCD6"), new SubCat("Academic & Exam Prep", "\uD83C\uDF93"),
                            new SubCat("Children's Books", "\uD83E\uDDF8"), new SubCat("Notebooks & Diaries", "\uD83D\uDCD3"),
                            new SubCat("Office Supplies", "\uD83D\uDCCE"), new SubCat("Art & Craft", "\uD83C\uDFA8")),
                    new SeedProduct("Bestseller Fiction Novel", "\uD83D\uDCD6", "Paperback, award-winning story", "8.99", "0", 100),
                    new SeedProduct("Ruled Notebook Pack of 6", "\uD83D\uDCD3", "200 pages each, spiral bound", "6.49", "5", 150));

            categoryRepository.flush();
        }
    }

    private void seedTopCategory(String name, String emoji, int paletteIdx, String description,
                                  List<SubCat> subs, SeedProduct... productsForFirstSub) {
        Category top = Category.builder()
                .name(name)
                .description(description)
                .imageUrl(catImage(name))
                .parentId(null)
                .build();
        top = categoryRepository.save(top);

        Category firstSub = null;
        for (int i = 0; i < subs.size(); i++) {
            SubCat sc = subs.get(i);
            Category sub = Category.builder()
                    .name(sc.name())
                    .description(name + " - " + sc.name())
                    .imageUrl(catImage(sc.name()))
                    .parentId(top.getId())
                    .build();
            sub = categoryRepository.save(sub);
            if (i == 0) firstSub = sub;
        }

        if (firstSub != null) {
            for (SeedProduct sp : productsForFirstSub) {
                Product product = Product.builder()
                        .name(sp.name())
                        .category(firstSub)
                        .description(sp.description())
                        .price(new BigDecimal(sp.price()))
                        .gstPercent(new BigDecimal(sp.gstPercent()))
                        .stockQty(sp.stock())
                        .imageUrl(productImage(sp.name()))
                        .active(true)
                        .build();
                product = productRepository.save(product);

                Stock stock = Stock.builder().product(product).quantity(sp.stock()).build();
                stockRepository.save(stock);
            }
        }
    }
}
