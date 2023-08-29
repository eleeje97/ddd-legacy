package kitchenpos;

import kitchenpos.application.ProductService;
import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.infra.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, menuRepository, purgomalumClient);
    }

    @Test
    void 상품을_등록한다() {
        // given
        final Product request = new Product();
        request.setName("후라이드 치킨");
        request.setPrice(BigDecimal.valueOf(16_000L));

        given(purgomalumClient.containsProfanity(any())).willReturn(false);

        final Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("후라이드 치킨");
        product.setPrice(BigDecimal.valueOf(16_000L));
        given(productRepository.save(any())).willReturn(product);

        // when
        final Product actual = productService.create(request);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void 상품의_가격은_0원_미만이면_예외가_발생한다() {
        // given
        final Product request = new Product();
        request.setName("후라이드 치킨");
        request.setPrice(BigDecimal.valueOf(-1L));

        // when, then
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
