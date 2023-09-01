package kitchenpos;

import kitchenpos.application.MenuGroupService;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MenuGroupServiceTest {
    @Mock
    private MenuGroupRepository menuGroupRepository;

    private MenuGroupService menuGroupService;

    private static final String DOUBLE_SET = "더블 세트";
    private static final String TRIPLE_SET = "트리플 세트";

    @BeforeEach
    void setUp() {
        menuGroupService = new MenuGroupService(menuGroupRepository);
    }

    @Test
    void 메뉴그룹을_생성한다() {
        // given
        MenuGroup request = new MenuGroup();
        request.setName(DOUBLE_SET);

        given(menuGroupRepository.save(any())).willReturn(createMenuGroup(DOUBLE_SET));

        // when
        MenuGroup actual = menuGroupService.create(request);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void 메뉴그룹이름이_빈값이면_예외가_발생한다(String menuGroupName) {
        // given
        MenuGroup request = new MenuGroup();
        request.setName(menuGroupName);

        // when, then
        assertThatThrownBy(() -> menuGroupService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메뉴그룹을_조회한다() {
        // given
        MenuGroup request1 = new MenuGroup();
        request1.setName(DOUBLE_SET);
        menuGroupService.create(request1);

        MenuGroup request2 = new MenuGroup();
        request2.setName(TRIPLE_SET);
        menuGroupService.create(request2);

        given(menuGroupRepository.findAll()).willReturn(List.of(createMenuGroup(DOUBLE_SET), createMenuGroup(TRIPLE_SET)));

        // when
        List<MenuGroup> menuGroupList = menuGroupService.findAll();

        // then
        assertThat(menuGroupList.size()).isEqualTo(2);
    }

    private MenuGroup createMenuGroup(String menuGroupName) {
        MenuGroup menuGroup = new MenuGroup();
        menuGroup.setId(UUID.randomUUID());
        menuGroup.setName(menuGroupName);
        return menuGroup;
    }

}
