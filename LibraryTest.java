import java.util.*;
import java.io.*;

// Exception Classes
class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) {
        super(message);
    }
}

class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) {
        super(message);
    }
}

// Book Class
class Book {
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    public Book(String title, String author, String genre) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isAvailable = true;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void checkout() throws BookNotAvailableException {
        if (!isAvailable) {
            throw new BookNotAvailableException("Book '" + title + "' is not available for checkout");
        }
        isAvailable = false;
    }

    public void returnBook() {
        isAvailable = true;
    }

    public String getGenre() {
        return genre;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + genre + ")" + 
               " - " + (isAvailable ? "Available" : "Checked Out");
    }
}

// Strategy Pattern for Sorting (Extra Credit)
interface SortingStrategy {
    void sort(List<Book> books);
}

class SortByTitle implements SortingStrategy {
    @Override
    public void sort(List<Book> books) {
        books.sort(Comparator.comparing(Book::getTitle));
    }
}

class SortByAuthor implements SortingStrategy {
    @Override
    public void sort(List<Book> books) {
        books.sort(Comparator.comparing(Book::getAuthor));
    }
}

class SortByGenre implements SortingStrategy {
    @Override
    public void sort(List<Book> books) {
        books.sort(Comparator.comparing(Book::getGenre));
    }
}

// Factory Pattern for Book Creation (Extra Credit)
class BookFactory {
    public static Book createBook(String type, String title, String author, String genre) {
        switch (type.toLowerCase()) {
            case "fiction":
            case "non-fiction":
            case "mystery":
            case "romance":
            case "sci-fi":
                return new Book(title, author, genre);
            default:
                return new Book(title, author, "General");
        }
    }
}

// Custom Iterator for Available Books
class AvailableBooksIterator implements Iterator<Book> {
    private List<Book> books;
    private int currentIndex;

    public AvailableBooksIterator(List<Book> books) {
        this.books = new ArrayList<>();
        // Only add available books to the iterator
        for (Book book : books) {
            if (book.isAvailable()) {
                this.books.add(book);
            }
        }
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < books.size();
    }

    @Override
    public Book next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more available books");
        }
        return books.get(currentIndex++);
    }
}

// LibraryCollection Class (Implements Iterable)
class LibraryCollection implements Iterable<Book> {
    private Map<String, List<Book>> genreMap;
    private SortingStrategy sortingStrategy;
    private String currentGenre; // For iterator implementation

    public LibraryCollection() {
        genreMap = new HashMap<>();
        sortingStrategy = new SortByTitle(); // Default sorting
        currentGenre = null;
    }

    public void setSortingStrategy(SortingStrategy strategy) {
        this.sortingStrategy = strategy;
    }

    public void addBook(Book book) {
        String genre = book.getGenre();
        genreMap.computeIfAbsent(genre, k -> new ArrayList<>()).add(book);
    }

    public Iterator<Book> getGenreIterator(String genre) {
        List<Book> booksInGenre = genreMap.getOrDefault(genre, new ArrayList<>());
        return new AvailableBooksIterator(booksInGenre);
    }

    // Set the current genre for the default iterator
    public void setCurrentGenre(String genre) {
        this.currentGenre = genre;
    }

    @Override
    public Iterator<Book> iterator() {
        if (currentGenre != null) {
            return getGenreIterator(currentGenre);
        }
        // If no current genre set, return iterator for all available books
        List<Book> allAvailableBooks = new ArrayList<>();
        for (List<Book> books : genreMap.values()) {
            for (Book book : books) {
                if (book.isAvailable()) {
                    allAvailableBooks.add(book);
                }
            }
        }
        return allAvailableBooks.iterator();
    }

    public Book findBook(String title) throws BookNotFoundException {
        for (List<Book> books : genreMap.values()) {
            for (Book book : books) {
                if (book.getTitle().equalsIgnoreCase(title)) {
                    return book;
                }
            }
        }
        throw new BookNotFoundException("Book '" + title + "' not found in library");
    }

    public Set<String> getAvailableGenres() {
        return genreMap.keySet();
    }

    public List<Book> getBooksInGenre(String genre) {
        return genreMap.getOrDefault(genre, new ArrayList<>());
    }

    public void sortBooksInGenre(String genre) {
        List<Book> books = genreMap.get(genre);
        if (books != null) {
            sortingStrategy.sort(books);
        }
    }

    // Extra Credit: Save library state to file
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, List<Book>> entry : genreMap.entrySet()) {
                for (Book book : entry.getValue()) {
                    writer.println(book.getTitle() + "," + book.getAuthor() + "," + 
                                 book.getGenre() + "," + book.isAvailable());
                }
            }
        }
    }

    // Extra Credit: Load library state from file
    public void loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Book book = new Book(parts[0], parts[1], parts[2]);
                    if (!Boolean.parseBoolean(parts[3])) {
                        try {
                            book.checkout();
                        } catch (BookNotAvailableException e) {
                            // Should not happen for new book
                        }
                    }
                    addBook(book);
                }
            }
        }
    }
}

// LibraryUser Class
class LibraryUser {
    private String name;
    private HashSet<Book> checkedOutBooks; // Extra Credit: HashSet to prevent duplicates

    public LibraryUser(String name) {
        this.name = name;
        this.checkedOutBooks = new HashSet<>();
    }

    public void borrowBook(Book book) throws BookNotAvailableException {
        if (checkedOutBooks.contains(book)) {
            throw new BookNotAvailableException("You have already checked out this book");
        }
        book.checkout();
        checkedOutBooks.add(book);
        System.out.println(name + " successfully checked out: " + book.getTitle());
    }

    public void returnBook(Book book) {
        if (checkedOutBooks.remove(book)) {
            book.returnBook();
            System.out.println(name + " successfully returned: " + book.getTitle());
        } else {
            System.out.println("You don't have this book checked out");
        }
    }

    public Set<Book> getCheckedOutBooks() {
        return new HashSet<>(checkedOutBooks);
    }

    public String getName() {
        return name;
    }
}

// Main Program
public class LibraryTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LibraryCollection library = new LibraryCollection();

        // Add sample books to library
        initializeLibrary(library);

        System.out.println("Welcome to the Digital Library System!");
        System.out.print("Enter your name: ");
        String userName = scanner.nextLine();
        LibraryUser user = new LibraryUser(userName);

        boolean running = true;
        while (running) {
            try {
                showMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        browseByGenre(library, scanner);
                        break;
                    case 2:
                        checkoutBook(library, user, scanner);
                        break;
                    case 3:
                        returnBook(library, user, scanner);
                        break;
                    case 4:
                        viewCheckedOutBooks(user);
                        break;
                    case 5:
                        setSortingStrategy(library, scanner);
                        break;
                    case 6:
                        saveLibraryState(library, scanner);
                        break;
                    case 7:
                        loadLibraryState(library, scanner);
                        break;
                    case 8:
                        running = false;
                        System.out.println("Thank you for using the Digital Library System!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear invalid input
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void initializeLibrary(LibraryCollection library) {
        // Fiction books
        library.addBook(BookFactory.createBook("fiction", "To Kill a Mockingbird", "Harper Lee", "Fiction"));
        library.addBook(BookFactory.createBook("fiction", "1984", "George Orwell", "Fiction"));
        library.addBook(BookFactory.createBook("fiction", "Pride and Prejudice", "Jane Austen", "Fiction"));

        // Mystery books
        library.addBook(BookFactory.createBook("mystery", "The Girl with the Dragon Tattoo", "Stieg Larsson", "Mystery"));
        library.addBook(BookFactory.createBook("mystery", "Gone Girl", "Gillian Flynn", "Mystery"));

        // Sci-Fi books
        library.addBook(BookFactory.createBook("sci-fi", "Dune", "Frank Herbert", "Sci-Fi"));
        library.addBook(BookFactory.createBook("sci-fi", "The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Sci-Fi"));

        // Non-Fiction books
        library.addBook(BookFactory.createBook("non-fiction", "Sapiens", "Yuval Noah Harari", "Non-Fiction"));
        library.addBook(BookFactory.createBook("non-fiction", "Educated", "Tara Westover", "Non-Fiction"));

        // Romance books
        library.addBook(BookFactory.createBook("romance", "The Notebook", "Nicholas Sparks", "Romance"));
        library.addBook(BookFactory.createBook("romance", "Me Before You", "Jojo Moyes", "Romance"));
    }

    private static void showMenu() {
        System.out.println("\n=== Library Menu ===");
        System.out.println("1. Browse books by genre");
        System.out.println("2. Checkout a book");
        System.out.println("3. Return a book");
        System.out.println("4. View your checked out books");
        System.out.println("5. Change sorting preference");
        System.out.println("6. Save library state");
        System.out.println("7. Load library state");
        System.out.println("8. Exit");
        System.out.print("Choose an option: ");
    }

    private static void browseByGenre(LibraryCollection library, Scanner scanner) {
        System.out.println("\nAvailable genres:");
        Set<String> genres = library.getAvailableGenres();
        for (String genre : genres) {
            System.out.println("- " + genre);
        }

        System.out.print("Enter genre to browse: ");
        String genre = scanner.nextLine();

        Iterator<Book> iterator = library.getGenreIterator(genre);
        System.out.println("\nAvailable books in " + genre + ":");
        
        boolean hasBooks = false;
        while (iterator.hasNext()) {
            System.out.println("- " + iterator.next());
            hasBooks = true;
        }
        
        if (!hasBooks) {
            System.out.println("No available books in this genre.");
        }
    }

    private static void checkoutBook(LibraryCollection library, LibraryUser user, Scanner scanner) {
        System.out.print("Enter the title of the book to checkout: ");
        String title = scanner.nextLine();

        try {
            Book book = library.findBook(title);
            user.borrowBook(book);
        } catch (BookNotFoundException | BookNotAvailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBook(LibraryCollection library, LibraryUser user, Scanner scanner) {
        Set<Book> checkedOutBooks = user.getCheckedOutBooks();
        if (checkedOutBooks.isEmpty()) {
            System.out.println("You have no books checked out.");
            return;
        }

        System.out.println("Your checked out books:");
        for (Book book : checkedOutBooks) {
            System.out.println("- " + book.getTitle());
        }

        System.out.print("Enter the title of the book to return: ");
        String title = scanner.nextLine();

        try {
            Book book = library.findBook(title);
            user.returnBook(book);
        } catch (BookNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewCheckedOutBooks(LibraryUser user) {
        Set<Book> checkedOutBooks = user.getCheckedOutBooks();
        if (checkedOutBooks.isEmpty()) {
            System.out.println("You have no books checked out.");
        } else {
            System.out.println("\nYour checked out books:");
            for (Book book : checkedOutBooks) {
                System.out.println("- " + book);
            }
        }
    }

    private static void setSortingStrategy(LibraryCollection library, Scanner scanner) {
        System.out.println("Choose sorting preference:");
        System.out.println("1. Sort by Title");
        System.out.println("2. Sort by Author");
        System.out.println("3. Sort by Genre");
        System.out.print("Enter choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                library.setSortingStrategy(new SortByTitle());
                System.out.println("Sorting preference set to Title");
                break;
            case 2:
                library.setSortingStrategy(new SortByAuthor());
                System.out.println("Sorting preference set to Author");
                break;
            case 3:
                library.setSortingStrategy(new SortByGenre());
                System.out.println("Sorting preference set to Genre");
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    private static void saveLibraryState(LibraryCollection library, Scanner scanner) {
        System.out.print("Enter filename to save library state: ");
        String filename = scanner.nextLine();
        try {
            library.saveToFile(filename);
            System.out.println("Library state saved successfully to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void loadLibraryState(LibraryCollection library, Scanner scanner) {
        System.out.print("Enter filename to load library state: ");
        String filename = scanner.nextLine();
        try {
            library.loadFromFile(filename);
            System.out.println("Library state loaded successfully from " + filename);
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }
}