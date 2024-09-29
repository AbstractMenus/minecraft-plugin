package ru.abstractmenus.services.proxy.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
/**
 * Data transfer object for proxy adapters
 *
 * Do not forget make changes in adapter if you change this class
 */
public class ChangeSkinDTO {
    private String playerName;
    private UUID playerUUID;
    private String texture;
    private String signature;
}
