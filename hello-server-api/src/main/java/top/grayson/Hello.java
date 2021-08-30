package top.grayson;

import lombok.*;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:27
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello {
    private String message;
    private String description;
}
