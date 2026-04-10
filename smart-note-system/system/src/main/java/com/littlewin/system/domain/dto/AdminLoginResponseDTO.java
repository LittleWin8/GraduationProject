package com.littlewin.system.domain.dto;

import com.littlewin.system.domain.vo.MenuVO;
import lombok.Data;
import java.util.List;

@Data
public class AdminLoginResponseDTO {
    private List<MenuVO> menuList;
    private List<String> authButtonList;
}