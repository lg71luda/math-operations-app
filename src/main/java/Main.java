import java.sql.*;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;

public class Main {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/math_operations_db";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "123456";

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		boolean running = true;

		while (running) {
			showMenu();
			System.out.print("Выберите пункт меню (1-10): ");
			int choice = scanner.nextInt();

			try {
				switch (choice) {
					case 1 -> showAllTables();
					case 2 -> createTable();
					case 3 -> performOperation(scanner, "Сложение", (a, b) -> a + b);
					case 4 -> performOperation(scanner, "Вычитание", (a, b) -> a - b);
					case 5 -> performOperation(scanner, "Умножение", (a, b) -> a * b);
					case 6 -> performOperation(scanner, "Деление", (a, b) -> {
						if (b == 0)
							throw new ArithmeticException("Деление на ноль!");
						return a / b;
					});
					case 7 -> performOperation(scanner, "Остаток от деления", (a, b) -> {
						if (b == 0)
							throw new ArithmeticException("Деление на ноль!");
						return a % b;
					});
					case 8 -> {
						System.out.print("Введите число для взятия модуля: ");
						double num = scanner.nextDouble();
						double result = Math.abs(num);
						saveResult("Модуль", num, 0, result);
						System.out.println("Результат: |" + num + "| = " + result);
					}
					case 9 -> {
						System.out.print("Введите основание: ");
						double base = scanner.nextDouble();
						System.out.print("Введите степень: ");
						double exp = scanner.nextDouble();
						double result = Math.pow(base, exp);
						saveResult("Возведение в степень", base, exp, result);
						System.out.println("Результат: " + base + " ^ " + exp + " = " + result);
					}
					case 10 -> {
						exportToExcel();
					}
					case 0 -> {
						running = false;
						System.out.println("Выход из программы.");
					}
					default -> System.out.println("Неверный выбор. Попробуйте снова.");
				}
			} catch (Exception e) {
				System.err.println("Ошибка: " + e.getMessage());
			}
		}
		scanner.close();
	}

	private static void showMenu() {
		System.out.println("\n=== Меню ===");
		System.out.println("1. Вывести все таблицы из MySQL");
		System.out.println("2. Создать таблицу в MySQL");
		System.out.println("3. Сложение чисел");
		System.out.println("4. Вычитание чисел");
		System.out.println("5. Умножение чисел");
		System.out.println("6. Деление чисел");
		System.out.println("7. Остаток от деления");
		System.out.println("8. Модуль числа");
		System.out.println("9. Возведение в степень");
		System.out.println("10. Экспорт в Excel");
		System.out.println("0. Выход");
	}

	private static void showAllTables() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

			System.out.println("Таблицы в базе данных:");
			while (rs.next()) {
				System.out.println("- " + rs.getString(1));
			}
		}
	}

	private static void createTable() throws SQLException {
		String sql = """
				CREATE TABLE IF NOT EXISTS operations (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				    operation VARCHAR(50) NOT NULL,
				    operand1 DOUBLE,
				    operand2 DOUBLE,
				    result DOUBLE,
				    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
				)
				""";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Таблица 'operations' создана или уже существует.");
		}
	}

	private static void performOperation(Scanner scanner, String opName, java.util.function.BinaryOperator<Double> op) {
		System.out.print("Введите первое число: ");
		double a = scanner.nextDouble();
		System.out.print("Введите второе число: ");
		double b = scanner.nextDouble();

		try {
			double result = op.apply(a, b);
			saveResult(opName, a, b, result);
			System.out.println("Результат: " + a + " и " + b + " → " + result);
		} catch (ArithmeticException e) {
			System.out.println("Ошибка: " + e.getMessage());
		}
	}

	private static void saveResult(String operation, double a, double b, double result) {
		String sql = "INSERT INTO operations (operation, operand1, operand2, result) VALUES (?, ?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, operation);
			pstmt.setDouble(2, a);
			pstmt.setDouble(3, b);
			pstmt.setDouble(4, result);
			pstmt.executeUpdate();
			System.out.println("Результат сохранён в базу данных.");
		} catch (SQLException e) {
			System.err.println("Ошибка сохранения в БД: " + e.getMessage());
		}
	}

	private static void exportToExcel() {
		String excelFile = "operations_export.xlsx";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM operations ORDER BY id")) {

			// Создаём книгу Excel
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Операции");

			// Заголовки
			Row headerRow = sheet.createRow(0);
			headerRow.createCell(0).setCellValue("ID");
			headerRow.createCell(1).setCellValue("Операция");
			headerRow.createCell(2).setCellValue("Операнд 1");
			headerRow.createCell(3).setCellValue("Операнд 2");
			headerRow.createCell(4).setCellValue("Результат");
			headerRow.createCell(5).setCellValue("Дата");

			// Данные
			int rowNum = 1;
			while (rs.next()) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(rs.getInt("id"));
				row.createCell(1).setCellValue(rs.getString("operation"));
				row.createCell(2).setCellValue(rs.getDouble("operand1"));
				row.createCell(3).setCellValue(rs.getDouble("operand2"));
				row.createCell(4).setCellValue(rs.getDouble("result"));
				row.createCell(5).setCellValue(rs.getTimestamp("created_at").toString());
			}

			// Автоподбор ширины столбцов
			for (int i = 0; i < 6; i++) {
				sheet.autoSizeColumn(i);
			}

			// Сохраняем файл
			try (FileOutputStream out = new FileOutputStream(excelFile)) {
				workbook.write(out);
			}
			workbook.close();

			System.out.println("Данные экспортированы в файл: " + new File(excelFile).getAbsolutePath());

		} catch (Exception e) {
			System.err.println("Ошибка экспорта в Excel: " + e.getMessage());
			e.printStackTrace();
		}
	}
}