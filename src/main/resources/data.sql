-- Insert sample topics
INSERT INTO topics (name, description, difficulty_level, duration_minutes, question_count) VALUES
('Java Programming', 'Core Java concepts including OOP, collections, and exception handling', 'BEGINNER', 30, 10),
('Spring Framework', 'Spring Boot, dependency injection, and web development', 'INTERMEDIATE', 45, 15),
('Database Management', 'SQL, database design, and normalization', 'BEGINNER', 25, 8),
('Web Development', 'HTML, CSS, JavaScript, and modern frameworks', 'INTERMEDIATE', 40, 12),
('Data Structures', 'Arrays, linked lists, trees, and algorithms', 'ADVANCED', 60, 20),
('System Design', 'Architecture patterns, scalability, and distributed systems', 'ADVANCED', 90, 25);

-- Insert sample questions for Java Programming
INSERT INTO questions (question_text, question_type, options, correct_answer, explanation, difficulty_level, points, topic_id) VALUES
('What is the default value of a boolean variable in Java?', 'MULTIPLE_CHOICE', 'A: true,B: false,C: null,D: 0', 'B', 'In Java, boolean variables are initialized to false by default.', 'BEGINNER', 1, 1),
('Which of the following is not a Java keyword?', 'MULTIPLE_CHOICE', 'A: instanceof,B: strictfp,C: friend,D: volatile', 'C', 'friend is not a Java keyword, it exists in C++.', 'BEGINNER', 1, 1),
('What is the size of int variable in Java?', 'MULTIPLE_CHOICE', 'A: 8 bits,B: 16 bits,C: 32 bits,D: 64 bits', 'C', 'int in Java is 32 bits (4 bytes) regardless of the platform.', 'BEGINNER', 1, 1),
('Which method must be implemented by a class implementing Runnable interface?', 'MULTIPLE_CHOICE', 'A: start(),B: run(),C: execute(),D: init()', 'B', 'The Runnable interface requires implementing the run() method.', 'BEGINNER', 1, 1),
('What is polymorphism in Java?', 'MULTIPLE_CHOICE', 'A: Multiple inheritance,B: Method overriding,C: Encapsulation,D: Abstraction', 'B', 'Polymorphism allows objects of different types to be treated as objects of a common superclass, typically through method overriding.', 'INTERMEDIATE', 2, 1),
('Which collection class allows duplicate elements and maintains insertion order?', 'MULTIPLE_CHOICE', 'A: HashSet,B: TreeSet,C: ArrayList,D: HashMap', 'C', 'ArrayList allows duplicates and maintains insertion order.', 'BEGINNER', 1, 1),
('What is the purpose of the finally block?', 'MULTIPLE_CHOICE', 'A: Handle exceptions,B: Always execute code,C: Return values,D: Break loops', 'B', 'The finally block always executes whether an exception is thrown or not.', 'BEGINNER', 1, 1),
('Which access modifier makes a member accessible only within the same class?', 'MULTIPLE_CHOICE', 'A: public,B: private,C: protected,D: default', 'B', 'private restricts access to the same class only.', 'BEGINNER', 1, 1),
('What is the output of: System.out.println(2 + 2 * 2);', 'MULTIPLE_CHOICE', 'A: 8,B: 6,C: 4,D: 2', 'B', 'Multiplication has higher precedence: 2 + (2 * 2) = 2 + 4 = 6.', 'BEGINNER', 1, 1),
('Which interface is used to sort objects in natural order?', 'MULTIPLE_CHOICE', 'A: Comparator,B: Comparable,C: Sortable,D: Orderable', 'B', 'Comparable defines natural ordering through the compareTo() method.', 'INTERMEDIATE', 2, 1);

-- Insert sample questions for Spring Framework
INSERT INTO questions (question_text, question_type, options, correct_answer, explanation, difficulty_level, points, topic_id) VALUES
('What annotation is used to mark a class as a Spring component?', 'MULTIPLE_CHOICE', 'A: @Component,B: @Service,C: @Repository,D: All of the above', 'D', 'All these annotations are stereotypes of @Component.', 'BEGINNER', 1, 2),
('What is dependency injection?', 'MULTIPLE_CHOICE', 'A: Manual object creation,B: Framework provides dependencies,C: Static imports,D: Reflection', 'B', 'DI is a process whereby objects define their dependencies through constructor arguments, factory methods, or properties.', 'INTERMEDIATE', 2, 2),
('Which annotation is used for auto-wiring dependencies?', 'MULTIPLE_CHOICE', 'A: @Inject,B: @Autowired,C: @Resource,D: All of the above', 'D', 'All these annotations can be used for dependency injection.', 'BEGINNER', 1, 2),
('What is the purpose of @SpringBootApplication?', 'MULTIPLE_CHOICE', 'A: Enable auto-configuration,B: Component scanning,C: Configuration class,D: All of the above', 'D', '@SpringBootApplication combines @EnableAutoConfiguration, @ComponentScan, and @Configuration.', 'INTERMEDIATE', 2, 2),
('Which stereotype annotation is specifically for service layer?', 'MULTIPLE_CHOICE', 'A: @Component,B: @Service,C: @Repository,D: @Controller', 'B', '@Service is specifically used for service layer beans.', 'BEGINNER', 1, 2);

-- Insert more sample questions for other topics
INSERT INTO questions (question_text, question_type, options, correct_answer, explanation, difficulty_level, points, topic_id) VALUES
('What does SQL stand for?', 'MULTIPLE_CHOICE', 'A: Structured Query Language,B: Simple Query Language,C: Standard Query Language,D: System Query Language', 'A', 'SQL stands for Structured Query Language.', 'BEGINNER', 1, 3),
('Which command is used to retrieve data from a database?', 'MULTIPLE_CHOICE', 'A: GET,B: SELECT,C: FETCH,D: RETRIEVE', 'B', 'SELECT is used to query data from database tables.', 'BEGINNER', 1, 3),
('What is HTML used for?', 'MULTIPLE_CHOICE', 'A: Styling web pages,B: Creating web page structure,C: Adding interactivity,D: Server-side programming', 'B', 'HTML defines the structure and content of web pages.', 'BEGINNER', 1, 4),
('Which CSS property changes the text color?', 'MULTIPLE_CHOICE', 'A: text-color,B: color,C: font-color,D: text-style', 'B', 'The color property sets the text color in CSS.', 'BEGINNER', 1, 4),
('What is the time complexity of binary search?', 'MULTIPLE_CHOICE', 'A: O(n),B: O(log n),C: O(n log n),D: O(1)', 'B', 'Binary search has logarithmic time complexity O(log n).', 'INTERMEDIATE', 2, 5),
('Which data structure uses LIFO principle?', 'MULTIPLE_CHOICE', 'A: Queue,B: Stack,C: Array,D: Linked List', 'B', 'Stack follows Last In First Out (LIFO) principle.', 'BEGINNER', 1, 5),
('What is microservices architecture?', 'MULTIPLE_CHOICE', 'A: Single monolithic application,B: Small independent services,C: Client-server model,D: Layered architecture', 'B', 'Microservices breaks down applications into small, independent services.', 'INTERMEDIATE', 2, 6),
('What is horizontal scaling?', 'MULTIPLE_CHOICE', 'A: Adding more resources to single server,B: Adding more servers,C: Optimizing code,D: Caching', 'B', 'Horizontal scaling involves adding more servers to handle load.', 'INTERMEDIATE', 2, 6);
