package proiect_bd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CarRentalApp {

    private static Connection connection;

    public static void main(String[] args) {
        // Configurăm fereastra principală
        JFrame mainFrame = new JFrame("Car Rental Park");
        mainFrame.setSize(400, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Adăugăm mesajul de bun venit
        JLabel welcomeLabel = new JLabel("Welcome to Car Rental Park", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 20));
        mainFrame.add(welcomeLabel, BorderLayout.NORTH);

        // Creăm panoul cu butoanele
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 2 rânduri și 2 coloane

        // Creăm butoanele pentru diferitele operațiuni
        JButton addCarButton = new JButton("Add Cars");
        JButton removeCarButton = new JButton("Remove Cars");
        JButton modifyCarButton = new JButton("Modify Cars");
        JButton showCarsButton = new JButton("Show Cars");

        buttonPanel.add(addCarButton);
        buttonPanel.add(removeCarButton);
        buttonPanel.add(modifyCarButton);
        buttonPanel.add(showCarsButton);

        mainFrame.add(buttonPanel, BorderLayout.CENTER);

        // Conectare la baza de date
        connectToDatabase();

        // Acțiuni pentru butoane
        addCarButton.addActionListener(e -> openAddCarWindow());
        removeCarButton.addActionListener(e -> openRemoveCarWindow());
        modifyCarButton.addActionListener(e -> openModifyCarWindow());
        showCarsButton.addActionListener(e -> openShowCarsWindow());

        // Afișăm fereastra principală
        mainFrame.setVisible(true);
    }

    // Funcție pentru conectarea la baza de date SQLite
    public static void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\User\\Desktop\\car_rental.db");
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    // Fereastra pentru adăugarea unei mașini
    public static void openAddCarWindow() {
        JFrame addCarFrame = new JFrame("Add Car");
        addCarFrame.setSize(300, 200);
        addCarFrame.setLayout(new FlowLayout());

        JLabel carNameLabel = new JLabel("Car Name:");
        JTextField carNameField = new JTextField(15);
        JLabel statusLabel = new JLabel("Status (Available/Rented):");
        JTextField statusField = new JTextField(15);
        JButton addButton = new JButton("Add");

        addCarFrame.add(carNameLabel);
        addCarFrame.add(carNameField);
        addCarFrame.add(statusLabel);
        addCarFrame.add(statusField);
        addCarFrame.add(addButton);

        addButton.addActionListener(e -> {
            String carName = carNameField.getText();
            String status = statusField.getText();
            if (!carName.isEmpty() && !status.isEmpty()) {
                addCarToDatabase(carName, status);
                JOptionPane.showMessageDialog(addCarFrame, "Car added successfully!");
                addCarFrame.dispose(); // Închidem fereastra Add Car
            } else {
                JOptionPane.showMessageDialog(addCarFrame, "Please fill in both fields.");
            }
        });

        addCarFrame.setVisible(true);
    }

    // Funcție pentru adăugarea unei mașini în baza de date
    public static void addCarToDatabase(String carName, String status) {
        String insertSQL = "INSERT INTO cars (name, status) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, carName);
            pstmt.setString(2, status);
            pstmt.executeUpdate();
            System.out.println("Car added to database.");
        } catch (SQLException e) {
            System.err.println("Failed to insert car: " + e.getMessage());
        }
    }

    // Fereastra pentru eliminarea unei mașini
    public static void openRemoveCarWindow() {
        JFrame removeCarFrame = new JFrame("Remove Car");
        removeCarFrame.setSize(300, 200);
        removeCarFrame.setLayout(new FlowLayout());

        // Obținem lista mașinilor din baza de date
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        getCarsList(model);

        JComboBox<String> carsComboBox = new JComboBox<>(model);
        JButton removeButton = new JButton("Remove");

        removeCarFrame.add(carsComboBox);
        removeCarFrame.add(removeButton);

        removeButton.addActionListener(e -> {
            String selectedCar = (String) carsComboBox.getSelectedItem();
            if (selectedCar != null) {
                removeCarFromDatabase(selectedCar);
                JOptionPane.showMessageDialog(removeCarFrame, "Car removed successfully!");
                removeCarFrame.dispose(); // Închidem fereastra Remove Car
            }
        });

        removeCarFrame.setVisible(true);
    }

    // Funcție pentru ștergerea unei mașini din baza de date
    public static void removeCarFromDatabase(String carName) {
        String deleteSQL = "DELETE FROM cars WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setString(1, carName);
            pstmt.executeUpdate();
            System.out.println("Car deleted from database.");
        } catch (SQLException e) {
            System.err.println("Failed to delete car: " + e.getMessage());
        }
    }

    // Funcție pentru obținerea listei de mașini
    public static void getCarsList(DefaultComboBoxModel<String> model) {
        String selectSQL = "SELECT name FROM cars";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                model.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch cars: " + e.getMessage());
        }
    }

    // Fereastra pentru modificarea statusului unei mașini
    public static void openModifyCarWindow() {
        JFrame modifyCarFrame = new JFrame("Modify Car");
        modifyCarFrame.setSize(300, 250);
        modifyCarFrame.setLayout(new FlowLayout());

        // Obținem lista mașinilor din baza de date
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        getCarsList(model);

        JComboBox<String> carsComboBox = new JComboBox<>(model);
        JTextField statusField = new JTextField(15);
        JLabel currentStatusLabel = new JLabel("Current Status:");
        JTextField currentStatusField = new JTextField(15);
        currentStatusField.setEditable(false);
        JButton modifyButton = new JButton("Modify");

        carsComboBox.addActionListener(e -> {
            String selectedCar = (String) carsComboBox.getSelectedItem();
            if (selectedCar != null) {
                // Obținem statusul curent al mașinii
                String currentStatus = getCarStatus(selectedCar);
                currentStatusField.setText(currentStatus);
            }
        });

        modifyCarFrame.add(carsComboBox);
        modifyCarFrame.add(currentStatusLabel);
        modifyCarFrame.add(currentStatusField);
        modifyCarFrame.add(statusField);
        modifyCarFrame.add(modifyButton);

        modifyButton.addActionListener(e -> {
            String selectedCar = (String) carsComboBox.getSelectedItem();
            String newStatus = statusField.getText();
            if (selectedCar != null && !newStatus.isEmpty()) {
                modifyCarStatusInDatabase(selectedCar, newStatus);
                JOptionPane.showMessageDialog(modifyCarFrame, "Car status modified successfully!");
                modifyCarFrame.dispose(); // Închidem fereastra Modify Car
            } else {
                JOptionPane.showMessageDialog(modifyCarFrame, "Please fill in both fields.");
            }
        });

        modifyCarFrame.setVisible(true);
    }

    // Funcție pentru obținerea statusului curent al unei mașini
    public static String getCarStatus(String carName) {
        String status = "";
        String selectSQL = "SELECT status FROM cars WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, carName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                status = rs.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch car status: " + e.getMessage());
        }
        return status;
    }

    // Funcție pentru modificarea statusului unei mașini în baza de date
    public static void modifyCarStatusInDatabase(String carName, String newStatus) {
        String updateSQL = "UPDATE cars SET status = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, carName);
            pstmt.executeUpdate();
            System.out.println("Car status updated.");
        } catch (SQLException e) {
            System.err.println("Failed to update car status: " + e.getMessage());
        }
    }

    // Fereastra pentru vizualizarea mașinilor
    public static void openShowCarsWindow() {
        JFrame showCarsFrame = new JFrame("Show Cars");
        showCarsFrame.setSize(400, 300);
        showCarsFrame.setLayout(new FlowLayout());

        // ComboBox pentru selecția statusului
        String[] options = {"All", "Available", "Rented"};
        JComboBox<String> statusComboBox = new JComboBox<>(options);
        JButton showButton = new JButton("Show");

        JTextArea displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);

        showCarsFrame.add(statusComboBox);
        showCarsFrame.add(showButton);
        showCarsFrame.add(new JScrollPane(displayArea));

        showButton.addActionListener(e -> {
            String statusFilter = (String) statusComboBox.getSelectedItem();
            displayCars(displayArea, statusFilter);
        });

        showCarsFrame.setVisible(true);
    }

    // Funcție pentru afișarea mașinilor în funcție de status
    public static void displayCars(JTextArea displayArea, String statusFilter) {
        String selectSQL = "SELECT * FROM cars";
        if (!statusFilter.equals("All")) {
            selectSQL += " WHERE status = ?";
        }
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            if (!statusFilter.equals("All")) {
                pstmt.setString(1, statusFilter);
            }
            ResultSet rs = pstmt.executeQuery();
            displayArea.setText(""); // Curățăm zona de text
            while (rs.next()) {
                String car = "ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") +
                             ", Status: " + rs.getString("status") + "\n";
                displayArea.append(car);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch cars: " + e.getMessage());
        }
    }
}
