package nl.tudelft.sem.template.example.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import nl.tudelft.sem.template.example.Application;
import nl.tudelft.sem.template.example.database.*;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdPutRequest;
import nl.tudelft.sem.template.model.BookshelfPostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.test.web.servlet.MvcResult;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@Transactional
public class SystemTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private BookTagRepository bookTagRepository;

    @Autowired
    private BookWrapperRepository bookWrapperRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void test() throws Exception {
        mvc.perform(get("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void createBookShelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        mvc.perform(post("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicBookshelves() throws Exception {
        mvc.perform(get("/bookshelf_service/bookshelf/get_public"))
                .andExpect(status().isNoContent());
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        mvc.perform(post("/bookshelf_service/bookshelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
        mvc.perform(get("/bookshelf_service/bookshelf/get_public"))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsAboutBookshelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("userId", userId.toString())).andReturn();
        String bookshelfId = result.getResponse().getContentAsString();
        String id = JsonPath.parse(bookshelfId).read("bookshelfId");
        mvc.perform(get("/bookshelf_service/bookshelf/{bookshelf_id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void editBookshelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("userId", userId.toString())).andReturn();
        String bookshelfId = result.getResponse().getContentAsString();
        String id = JsonPath.parse(bookshelfId).read("bookshelfId");

        BookshelfBookshelfIdPutRequest change = new BookshelfBookshelfIdPutRequest()
                .title("title1")
                .description("description1")
                .privacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PRIVATE);

        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}", id)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(change)))
                .andExpect(status().isOk());
    }

    @Test
    void editTitleBookshelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("userId", userId.toString())).andReturn();
        String bookshelfId = result.getResponse().getContentAsString();
        String id = JsonPath.parse(bookshelfId).read("bookshelfId");



        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}/edit/title", id)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + "title1" + "\""))
                .andExpect(status().isOk());
    }

    @Test
    void editDescriptionBookshelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("userId", userId.toString())).andReturn();
        String bookshelfId = result.getResponse().getContentAsString();
        String id = JsonPath.parse(bookshelfId).read("bookshelfId");



        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}/edit/description", id)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + "description1" + "\""))
                .andExpect(status().isOk());
    }

    @Test
    void editVisibilityBookshelf() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + userId + "\""));

        BookshelfPostRequest request = new BookshelfPostRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("userId", userId.toString())).andReturn();
        String bookshelfId = result.getResponse().getContentAsString();
        String id = JsonPath.parse(bookshelfId).read("bookshelfId");



        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}/edit/privacy", id)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + "PRIVATE" + "\""))
                .andExpect(status().isOk());
    }

//    @Test
//    void addBookToShelf() throws Exception {
//        //create user
//        UUID userId = UUID.randomUUID();
//        mvc.perform(post("/bookshelf_service/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("\"" + userId + "\""));
//
//        BookshelfPostRequest request = new BookshelfPostRequest()
//                .title("title")
//                .description("description")
//                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
//
//        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .param("userId", userId.toString())).andReturn();
//        String bookshelfId = result.getResponse().getContentAsString();
//        String id = JsonPath.parse(bookshelfId).read("bookshelfId");
//
//        Book book = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title")
//                .authors(List.of("author"))
//                .description("description")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//        MvcResult bookPutResult = mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book))).andReturn();
//
//        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}/book", id)
//                        .param("userId", userId.toString())
//                        .param("bookId", book.getBookId().toString()))
//                .andExpect(status().isOk());
//    }

//    @Test
//    void deleteBookToShelf() throws Exception {
//        //create user
//        UUID userId = UUID.randomUUID();
//        mvc.perform(post("/bookshelf_service/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("\"" + userId + "\""));
//
//        BookshelfPostRequest request = new BookshelfPostRequest()
//                .title("title")
//                .description("description")
//                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
//
//        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .param("userId", userId.toString())).andReturn();
//        String bookshelfId = result.getResponse().getContentAsString();
//        String id = JsonPath.parse(bookshelfId).read("bookshelfId");
//
//        Book book = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title")
//                .authors(List.of("author"))
//                .description("description")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//        MvcResult bookPutResult = mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book))).andReturn();
//
//        mvc.perform(put("/bookshelf_service/bookshelf/{bookshelfId}/book", id)
//                        .param("userId", userId.toString())
//                        .param("bookId", book.getBookId().toString()));
//
//        mvc.perform(delete("/bookshelf_service/bookshelf/{bookshelfId}/book", id)
//                        .param("userId", userId.toString())
//                        .param("bookId", book.getBookId().toString()))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void addMultipleBooksToShelf() throws Exception {
//        //create user
//        UUID userId = UUID.randomUUID();
//        mvc.perform(post("/bookshelf_service/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("\"" + userId + "\""));
//
//        BookshelfPostRequest request = new BookshelfPostRequest()
//                .title("title")
//                .description("description")
//                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
//
//        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .param("userId", userId.toString())).andReturn();
//        String bookshelfId = result.getResponse().getContentAsString();
//        String id = JsonPath.parse(bookshelfId).read("bookshelfId");
//
//        Book book1 = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title1")
//                .authors(List.of("author1"))
//                .description("description1")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//        Book book2 = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title2")
//                .authors(List.of("author2"))
//                .description("description2")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//
//        mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book1))).andReturn();
//
//        mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book2))).andReturn();
//
//        mvc.perform(put("/bookshelf_service/bookshelf/" + id + "/" + userId + "/book/add_multiple")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(List.of(book1.getBookId(), book2.getBookId()))))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void deletingMultipleBooksToShelf() throws Exception {
//        //create user
//        UUID userId = UUID.randomUUID();
//        mvc.perform(post("/bookshelf_service/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("\"" + userId + "\""));
//
//        BookshelfPostRequest request = new BookshelfPostRequest()
//                .title("title")
//                .description("description")
//                .privacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
//
//        MvcResult result = mvc.perform(post("/bookshelf_service/bookshelf")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .param("userId", userId.toString())).andReturn();
//        String bookshelfId = result.getResponse().getContentAsString();
//        String id = JsonPath.parse(bookshelfId).read("bookshelfId");
//
//        Book book1 = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title1")
//                .authors(List.of("author1"))
//                .description("description1")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//        Book book2 = new Book()
//                .bookId(UUID.randomUUID())
//                .title("title2")
//                .authors(List.of("author2"))
//                .description("description2")
//                .genres(List.of(Book.GenresEnum.CRIME))
//                .numPages(42);
//
//
//        mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book1))).andReturn();
//
//        mvc.perform(post("/bookshelf_service/catalog")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(book2))).andReturn();
//
//        mvc.perform(put("/bookshelf_service/bookshelf/" + id + "/" + userId + "/book/add_multiple")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(List.of(book1.getBookId(), book2.getBookId()))))
//                .andExpect(status().isOk());
//
//        mvc.perform(put("/bookshelf_service/bookshelf/" + id + "/" + userId + "/book/delete_multiple")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(List.of(book1.getBookId(), book2.getBookId()))))
//                .andExpect(status().isOk());
//    }

    @Test
    void postUserTest() throws Exception {
        //create user
        UUID userId = UUID.randomUUID();
        mvc.perform(post("/bookshelf_service/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\""+userId+"\""))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBooksTest() throws Exception {
        mvc.perform(get("/bookshelf_service/catalog"))
                    .andExpect(status().isNoContent());
        Book book = new Book()
                .title("title")
                .authors(List.of("author"))
                .description("description")
                .bookId(UUID.fromString("ccb429f2-6169-437b-9b41-deac01c8f34c")).numPages(100).genres(List.of(Book.GenresEnum.ROMANCE));
        mvc.perform(post("/bookshelf_service/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
        mvc.perform(get("/bookshelf_service/catalog"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBookTest() throws Exception {
        mvc.perform(delete("/bookshelf_service/catalog")
                .param("bookId", "ccb429f2-6169-437b-9b41-deac01c8f34c"))
                .andExpect(status().isNotFound());
        Book book = new Book()
                .title("title")
                .authors(List.of("author"))
                .description("description")
                .bookId(UUID.fromString("ccb429f2-6169-437b-9b41-deac01c8f34c")).numPages(100).genres(List.of(Book.GenresEnum.ROMANCE));
        mvc.perform(post("/bookshelf_service/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
        mvc.perform(delete("/bookshelf_service/catalog")
                        .param("bookId", "ccb429f2-6169-437b-9b41-deac01c8f34c"))
                        .andExpect(status().isOk());
        mvc.perform(delete("/bookshelf_service/catalog")
                        .param("bookId", "c37b-9b41-deac01c8f34c"))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void editBookTest() throws Exception {
        Book book = new Book()
                .title("title")
                .authors(List.of("author"))
                .description("description")
                .bookId(UUID.fromString("ccb429f2-6169-437b-9b41-deac01c8f34c")).numPages(100).genres(List.of(Book.GenresEnum.ROMANCE));

        mvc.perform(put("/bookshelf_service/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/bookshelf_service/catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());

        mvc.perform(get("/bookshelf_service/catalog/ccb429f2-6169-437b-9b41-deac01c8f34c"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("title"));
        book.setTitle("new title");
        mvc.perform(put("/bookshelf_service/catalog")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
        mvc.perform(get("/bookshelf_service/catalog/ccb429f2-6169-437b-9b41-deac01c8f34c"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("new title"));
    }

    @Test
    void searchBookTest() throws Exception {
        mvc.perform(get("/bookshelf_service/catalog/search")
                .param("title", "title")
                .param("author", "author"))
                .andExpect(status().isNoContent());
        Book book = new Book()
                .title("title")
                .authors(List.of("author"))
                .description("description")
                .bookId(UUID.fromString("ccb429f2-6169-437b-9b41-deac01c8f34c")).numPages(100).genres(List.of(Book.GenresEnum.ROMANCE));
        mvc.perform(post("/bookshelf_service/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());
        mvc.perform(get("/bookshelf_service/catalog/search")
                .param("title", "title")
                .param("author", "author1"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/bookshelf_service/catalog/search")
                        .param("title", "title1")
                        .param("author", "author"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/bookshelf_service/catalog/search")
                        .param("title", "title")
                        .param("author", "author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("ccb429f2-6169-437b-9b41-deac01c8f34c"));
    }
}
