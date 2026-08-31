package mobile.posts

/*
 * Exercise 4. Teach Hilt to build what it CANNOT construct on its own. See docs/lab8.md, Part 2
 * ("@Module and @Provides") and exercise 4.
 *
 * Hilt can build any class you mark `@Inject constructor`. It cannot build the Room database or the
 * Retrofit API, because those come from BUILDERS, not constructors. So you provide them here.
 *
 * Write an `object AppModule` that is a Hilt `@Module` installed in `SingletonComponent`, exposing a
 * `@Provides` function for each thing the graph needs but cannot build itself: the `AppDatabase`, the
 * `PostsDao` (the database hands it out), and the `PostsApi`.
 *
 * You already wrote the Room builder in LAB 6 and the Retrofit setup in LAB 5 - look back at your own
 * code and move that construction here, behind `@Provides`. Then decide, and be ready to defend in the
 * review: which of these should be `@Singleton`, and which should not? (Part 2, "Scoping: less is
 * more.") That decision is the point of the exercise, not the annotations.
 */
