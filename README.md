# 💰 Expense Tracker App

A modern, colorful, and vibrant expense tracking application built with React frontend and Spring Boot backend.

## ✨ Features

- 🔐 **User Authentication** - Secure JWT-based login and registration
- 💸 **Expense & Income Tracking** - Track both expenses and income
- 🏷️ **Category Management** - Pre-defined and custom categories
- 📊 **Monthly Reports** - Visual charts and analytics
- 📱 **Responsive Design** - Works on desktop and mobile
- 🎨 **Colorful UI** - Vibrant and modern design

## 🛠️ Tech Stack

### Frontend
- React 18
- React Router DOM
- Axios for API calls
- Lucide React for icons
- Recharts for data visualization
- React Hook Form for forms
- Styled Components

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven build system

## 🚀 Getting Started

### Prerequisites

- Node.js 16+ and npm
- Java 17+
- Maven 3.6+
- MySQL 8+
- Git

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd expense-tracker-app2
   ```

2. **Setup MySQL Database**
   ```sql
   CREATE DATABASE expense_tracker_db;
   ```

3. **Configure Database**
   Update `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   ```

4. **Run Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   
   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm start
   ```
   
   The frontend will start on `http://localhost:3000`

## 📱 Usage

### Getting Started

1. **Register** a new account or **Login** with existing credentials
2. **Dashboard** shows your financial overview
3. **Add Transactions** to track income and expenses
4. **View Reports** for monthly analytics
5. **Manage Categories** to organize your transactions

### Default Categories

The app comes with pre-defined categories:

**Expense Categories:**
- Food & Dining
- Transportation
- Shopping
- Entertainment
- Bills & Utilities
- Healthcare
- Education
- Travel
- Personal Care
- Home & Garden

**Income Categories:**
- Salary
- Freelance
- Business
- Investment
- Bonus
- Gift
- Other Income

## 🎨 Design Features

- **Gradient Backgrounds** - Beautiful color gradients throughout
- **Glassmorphism Effects** - Modern frosted glass design
- **Vibrant Colors** - Carefully chosen color palette
- **Smooth Animations** - Hover effects and transitions
- **Mobile Responsive** - Optimized for all screen sizes

## 🔧 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/validate` - Token validation

### Categories
- `GET /api/categories` - Get user categories
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Transactions
- `GET /api/transactions` - Get user transactions
- `POST /api/transactions` - Create new transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Reports
- `GET /api/reports/monthly/{year}/{month}` - Monthly summary
- `GET /api/reports/category-wise/{year}/{month}` - Category breakdown

## 🔒 Security

- JWT-based authentication
- Password encryption with BCrypt
- CORS configuration for frontend
- Input validation and sanitization

## 📊 Database Schema

### Users Table
- id, username, email, password, full_name
- created_at, updated_at

### Categories Table
- id, name, description, type (INCOME/EXPENSE)
- color, user_id, is_default
- created_at, updated_at

### Transactions Table
- id, amount, description, transaction_date
- type (INCOME/EXPENSE), category_id, user_id
- notes, created_at, updated_at

## 🛠️ Build Commands

### Backend (Maven)
```bash
# Compile
mvn compile

# Run tests
mvn test

# Package JAR
mvn package

# Run application
mvn spring-boot:run

# Clean build
mvn clean install
```

### Frontend (npm)
```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🐛 Known Issues

- None currently reported

## 🔮 Future Enhancements

- [ ] Budget tracking and alerts
- [ ] Export data to CSV/PDF
- [ ] Multiple currency support
- [ ] Recurring transactions
- [ ] Data visualization improvements
- [ ] Mobile app (React Native)

## 📞 Support

For support, please open an issue in the GitHub repository.

---

Made with ❤️ and lots of ☕ 