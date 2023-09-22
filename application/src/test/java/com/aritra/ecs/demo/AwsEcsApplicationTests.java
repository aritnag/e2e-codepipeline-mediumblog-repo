package com.aritra.ecs.demo;

import com.aritra.ecs.demo.controller.AuthController;
import com.aritra.ecs.demo.controller.CategoryController;
import com.aritra.ecs.demo.controller.HealthCheckController;
import com.aritra.ecs.demo.controller.ProductController;
import com.aritra.ecs.demo.dao.CategoryDao;
import com.aritra.ecs.demo.dao.ProductDao;
import com.aritra.ecs.demo.dao.UserDao;
import com.aritra.ecs.demo.entity.Category;
import com.aritra.ecs.demo.entity.Product;
import com.aritra.ecs.demo.inputs.*;
import com.aritra.ecs.demo.security.JwtTokenProvider;
import com.aritra.ecs.demo.service.AuthService;
import com.aritra.ecs.demo.service.CategoryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:drop_all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = {"classpath:schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AWSEcsDemoApplicationTests {
    @Autowired
    @Qualifier("categories")
    private CategoryDao categoryDao;

    @Autowired
    @Qualifier("products")
    private ProductDao productDao;

    @Autowired
    @Qualifier("users")
    private UserDao userDao;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryController categoryController;

    @Autowired
    private AuthController authController;

    @Autowired
    private ProductController productController;

    @Autowired
    private HealthCheckController healthCheckController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
        assertThat(userDao).isNotNull();
        assertThat(categoryDao).isNotNull();
        assertThat(productDao).isNotNull();
        assertThat(authService).isNotNull();
        assertThat(categoryService).isNotNull();
        assertThat(categoryController).isNotNull();
        assertThat(authController).isNotNull();
        assertThat(productController).isNotNull();
        assertThat(healthCheckController).isNotNull();
    }

    @Test
    public void healthCheckShouldReturnOK() throws Exception {
        this.mockMvc.perform(get("/health")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ok")));
    }

    @Test
    public void shouldReturnUnAuthorizedWithoutAuthToken() throws Exception {
        this.mockMvc.perform(get("/categories")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldSignUpAndReturnsOK() throws Exception {
        SignupInput input = new SignupInput("ozer", "123456");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(post("/auth/signup").
                contentType(MediaType.APPLICATION_JSON).
                content(json)).
                andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void shouldReturnBadCredentialsWhenCredentialsNotMatched() throws Exception {
        SigninInput signinInput = new SigninInput("ozer", "123456");
        ObjectMapper signInObjectMapper = new ObjectMapper();
        String signInInputJson = signInObjectMapper.writeValueAsString(signinInput);

        MvcResult result = mockMvc.perform(post("/auth/login").
                contentType(MediaType.APPLICATION_JSON).
                content(signInInputJson)).
                andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString().contains("Bad Credentials")).isTrue();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }


    @Test
    public void shouldSignUpLoginAndReturnsValidJWT() throws Exception {
        SignupInput signupInput = new SignupInput("ozer", "123456");
        ObjectMapper objectMapper = new ObjectMapper();
        String signUpInputJson = objectMapper.writeValueAsString(signupInput);

        mockMvc.perform(post("/auth/signup").
                contentType(MediaType.APPLICATION_JSON).
                content(signUpInputJson)).
                andExpect(status().isOk())
                .andReturn();

        SigninInput signinInput = new SigninInput("ozer", "123456");
        ObjectMapper signInObjectMapper = new ObjectMapper();
        String signInInputJson = signInObjectMapper.writeValueAsString(signinInput);

        MvcResult result = mockMvc.perform(post("/auth/login").
                contentType(MediaType.APPLICATION_JSON).
                content(signInInputJson)).
                andExpect(status().isOk())
                .andReturn();
        // Check response is not empty and it isn't bad credentials error, it's a jwt.
        assertThat(result.getResponse().getContentAsString().contains("Bad credentials")).isFalse();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        // Check token is valid!
        Boolean isTokenValid = jwtTokenProvider.validateToken(result.getResponse().getContentAsString());
        assertThat(isTokenValid).isTrue();
    }

    @Test
    public void shouldAddListUpdateCategoriesWithValidToken() throws Exception {
        SignupInput signupInput = new SignupInput("ozer", "123456");
        ObjectMapper objectMapper = new ObjectMapper();
        String signUpInputJson = objectMapper.writeValueAsString(signupInput);

        mockMvc.perform(post("/auth/signup").
                contentType(MediaType.APPLICATION_JSON).
                content(signUpInputJson)).
                andExpect(status().isOk())
                .andReturn();

        SigninInput signinInput = new SigninInput("ozer", "123456");
        ObjectMapper signInObjectMapper = new ObjectMapper();
        String signInInputJson = signInObjectMapper.writeValueAsString(signinInput);

        MvcResult loginResult = mockMvc.perform(post("/auth/login").
                contentType(MediaType.APPLICATION_JSON).
                content(signInInputJson)).
                andExpect(status().isOk())
                .andReturn();

        String jwtToken = loginResult.getResponse().getContentAsString();
        Boolean isTokenValid = jwtTokenProvider.validateToken(jwtToken);
        assertThat(isTokenValid).isTrue();

        // Add Category
        AddCategoryInput addCategoryInput = new AddCategoryInput();
        String categoryName = "toys";
        addCategoryInput.setName(categoryName);
        ObjectMapper addCategoryObjectMapper = new ObjectMapper();
        String addCategoryInputJson = addCategoryObjectMapper.writeValueAsString(addCategoryInput);

        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addCategoryInputJson))
                .andExpect(status().isOk())
                .andReturn();

        // List categories
        MvcResult listCategoriesResult = mockMvc.perform(get("/categories")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();

        ObjectMapper categoryListMapper = new ObjectMapper();
        List<Category> categories = Arrays.asList(categoryListMapper.readValue(listCategoriesResult.getResponse().getContentAsString(), Category[].class));
        Category category = categories.get(0);
        assertThat(category.getName()).isEqualTo(categoryName);

        // UpdateCategory
        String newCategoryName = "electronics";
        UpdateCategoryInput updateCategoryInput = new UpdateCategoryInput();
        updateCategoryInput.setName(newCategoryName);
        String updateCategoryInputJson = new ObjectMapper().writeValueAsString(updateCategoryInput);

        mockMvc.perform(put("/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCategoryInputJson))
                .andExpect(status().isOk()).andReturn();

        // Get CategoryById
        MvcResult categoryByIdResult = mockMvc.perform(get("/categories/" + category.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();
        ObjectMapper categoryByIdObjectMapper = new ObjectMapper();
        Category category1 = categoryByIdObjectMapper.readValue(categoryByIdResult.getResponse().getContentAsString(), Category.class);
        assertThat(category1.getName()).isEqualTo(newCategoryName);

        // Add Product & Get Products by categoryId
        AddProductInput addProductInput = new AddProductInput();
        String productName = "apple macbook pro 16inch";
        addProductInput.setName(productName);
        addProductInput.setPrice(100.50);
        addProductInput.setCategory_id(category.getId());

        String addProductInputJson = new ObjectMapper().writeValueAsString(addProductInput);

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addProductInputJson))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult productsByCategoryIdResult = mockMvc.perform(get("/categories/" + category.getId() + "/products")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();
        List<Product> products = Arrays.asList(categoryListMapper.readValue(productsByCategoryIdResult.getResponse().getContentAsString(), Product[].class));
        Product product = products.get(0);
        assertThat(product.getName()).isEqualTo(productName);
    }

    @Test
    public void shouldAddUpdateDeleteListProducts() throws Exception {
        SignupInput signupInput = new SignupInput("ozer", "123456");
        ObjectMapper objectMapper = new ObjectMapper();
        String signUpInputJson = objectMapper.writeValueAsString(signupInput);

        mockMvc.perform(post("/auth/signup").
                contentType(MediaType.APPLICATION_JSON).
                content(signUpInputJson)).
                andExpect(status().isOk())
                .andReturn();

        SigninInput signinInput = new SigninInput("ozer", "123456");
        ObjectMapper signInObjectMapper = new ObjectMapper();
        String signInInputJson = signInObjectMapper.writeValueAsString(signinInput);

        MvcResult loginResult = mockMvc.perform(post("/auth/login").
                contentType(MediaType.APPLICATION_JSON).
                content(signInInputJson)).
                andExpect(status().isOk())
                .andReturn();

        String jwtToken = loginResult.getResponse().getContentAsString();
        Boolean isTokenValid = jwtTokenProvider.validateToken(jwtToken);
        assertThat(isTokenValid).isTrue();

        // Add Category
        AddCategoryInput addCategoryInput = new AddCategoryInput();
        String categoryName = "toys";
        addCategoryInput.setName(categoryName);
        ObjectMapper addCategoryObjectMapper = new ObjectMapper();
        String addCategoryInputJson = addCategoryObjectMapper.writeValueAsString(addCategoryInput);

        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addCategoryInputJson))
                .andExpect(status().isOk())
                .andReturn();

        // List categories
        MvcResult listCategoriesResult = mockMvc.perform(get("/categories")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();

        ObjectMapper categoryListMapper = new ObjectMapper();
        List<Category> categories = Arrays.asList(categoryListMapper.readValue(listCategoriesResult.getResponse().getContentAsString(), Category[].class));
        Category category = categories.get(0);
        assertThat(category.getName()).isEqualTo(categoryName);

        // Add Products
        AddProductInput addProductInput = new AddProductInput();
        String productName = "apple macbook pro 16inch";
        addProductInput.setName(productName);
        addProductInput.setPrice(100.50);
        addProductInput.setCategory_id(category.getId());

        String addProductInputJson = new ObjectMapper().writeValueAsString(addProductInput);

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addProductInputJson))
                .andExpect(status().isOk())
                .andReturn();

        AddProductInput addProductInput2 = new AddProductInput();
        String productName2 = "apple magic keyboard";
        double productPrice2 = 30.50;
        addProductInput2.setName(productName2);
        addProductInput2.setPrice(productPrice2);
        addProductInput2.setCategory_id(category.getId());

        String addProductInputJson2 = new ObjectMapper().writeValueAsString(addProductInput2);

        mockMvc.perform(post("/products").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addProductInputJson2))
                .andExpect(status().isOk())
                .andReturn();

        // List 2 Products By CategoryId
        MvcResult productsByCategoryIdResult = mockMvc.perform(get("/categories/" + category.getId() + "/products")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();
        List<Product> products = Arrays.asList(categoryListMapper.readValue(productsByCategoryIdResult.getResponse().getContentAsString(), Product[].class));
        assertThat(products.size()).isEqualTo(2);

        Product product1 = products.get(0);
        assertThat(product1.getName()).isEqualTo(productName);

        Product product2 = products.get(1);
        assertThat(product2.getPrice()).isEqualTo(productPrice2);

        // Update ProductById
        String newProductName1 = "apple macbook pro 13inch";
        double newProduct1Price = 50.50;
        UpdateProductInput updateProductInput = new UpdateProductInput();
        updateProductInput.setCategory_id(category.getId());
        updateProductInput.setName(newProductName1);
        updateProductInput.setPrice(newProduct1Price);
        String updateProductInputJson = new ObjectMapper().writeValueAsString(updateProductInput);

        mockMvc.perform(put("/products/" + product1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateProductInputJson))
                .andExpect(status().isOk()).andReturn();

        // Get Updated Product By Id
        MvcResult productByIdResult = mockMvc.perform(get("/products/" + product1.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();
        ObjectMapper categoryByIdObjectMapper = new ObjectMapper();
        Product product1ById = categoryByIdObjectMapper.readValue(productByIdResult.getResponse().getContentAsString(), Product.class);

        assertThat(product1ById.getPrice()).isEqualTo(newProduct1Price);
        assertThat(product1ById.getName()).isEqualTo(newProductName1);

        // Delete Product By Id
        mockMvc.perform(delete("/products/" + product2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();


        MvcResult getDeletedProductByIdResult = mockMvc.perform(get("/products/" + product2.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()).andReturn();

        assertThat(getDeletedProductByIdResult.getResponse().getContentAsString()).isEqualTo("");
    }
}
