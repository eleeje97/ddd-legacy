package kitchenpos.application;

import kitchenpos.application.ProductService;
import kitchenpos.domain.*;
import kitchenpos.infra.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PurgomalumClient purgomalumClient;

    private ProductService productService;

    private static final String FRIED_CHICKEN = "후라이드 치킨";

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, menuRepository, purgomalumClient);
    }

    @Test
    void 상품을_등록한다() {
        // given
        final Product product = createProduct(FRIED_CHICKEN, 16_000L);
        given(purgomalumClient.containsProfanity(any())).willReturn(false);
        given(productRepository.save(any())).willReturn(product);

        // when
        final Product actual = productService.create(product);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"fuck chicken", "shit chicken"})
    void 상품의_이름에_비속어가_있으면_예외가_발생한다(String productName) {
        // given
        final Product product = createProduct(productName);
        given(purgomalumClient.containsProfanity(any())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품의_가격은_0원_미만이면_예외가_발생한다() {
        // given
        final Product product = createProduct(-1L);

        // when, then
        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품가격을_수정한다() {
        // given
        final Product product = createProduct(16_000L);
        given(productRepository.findById(any())).willReturn(Optional.of(product));

        // when
        product.setPrice(BigDecimal.valueOf(18_000L));
        final Product actual = productService.changePrice(product.getId(), product);

        // then
        assertThat(actual.getPrice()).isEqualTo(BigDecimal.valueOf(18_000L));
    }

    @Test
    void 수정할_상품가격이_0원_미만이면_예외가_발생한다() {
        // given
        final Product product = createProduct();

        // when, then
        product.setPrice(BigDecimal.valueOf(-1L));
        assertThatThrownBy(() -> productService.changePrice(product.getId(), product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메뉴의_가격이_메뉴에_속한_상품금액의_합보다_크면_메뉴가_숨겨진다() {
        // given
        final Product product = createProduct();
        final MenuProduct menuProduct = createMenuProduct(product);
        final Menu menu = createMenu("후라이드 1+1", 32_000L, List.of(menuProduct));
        given(productRepository.findById(any())).willReturn(Optional.of(product));
        given(menuRepository.findAllByProductId(any())).willReturn(List.of(menu));

        // when
        product.setPrice(BigDecimal.valueOf(15_000L));
        productService.changePrice(product.getId(), product);

        // then
        assertThat(menu.isDisplayed()).isFalse();
    }

    @Test
    void 상품목록을_조회한다() {
        // given
        Product product1 = createProduct();
        Product product2 = createProduct();
        productService.create(product1);
        productService.create(product2);

        given(productRepository.findAll()).willReturn(List.of(product1, product2));

        // when
        List<Product> products = productService.findAll();

        // then
        assertThat(products.size()).isEqualTo(2);
    }


    private Product createProduct(String productName, Long price) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(productName);
        product.setPrice(BigDecimal.valueOf(price));
        return product;
    }

    private Product createProduct(String productName) {
        return createProduct(productName, 16_000L);
    }

    private Product createProduct(Long price) {
        return createProduct(FRIED_CHICKEN, price);
    }

    private Product createProduct() {
        return createProduct(FRIED_CHICKEN, 16_000L);
    }

    private MenuProduct createMenuProduct(Product product) {
        final MenuProduct menuProduct = new MenuProduct();
        menuProduct.setSeq(1L);
        menuProduct.setProduct(product);
        menuProduct.setQuantity(2L);
        return menuProduct;
    }

    private Menu createMenu(String menuName, Long menuPrice, List<MenuProduct> menuProducts) {
        final Menu menu = new Menu();
        menu.setId(UUID.randomUUID());
        menu.setName(menuName);
        menu.setPrice(BigDecimal.valueOf(menuPrice));
        menu.setDisplayed(true);
        menu.setMenuProducts(menuProducts);
        return menu;
    }
}
