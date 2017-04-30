package com.faforever.gw.services.messaging.outgoing;

import com.faforever.gw.security.User;
import com.faforever.gw.services.messaging.AbstractOutgoingWebSocketMessage;
import com.faforever.gw.services.messaging.MessageType;
import lombok.Data;

import java.util.Collection;
import java.util.UUID;

@Data
public class CharacterPromotionMessage extends AbstractOutgoingWebSocketMessage {
    private UUID character;
    private int newRank;

    public CharacterPromotionMessage(Collection<User> userList, UUID characterId, int newRank) {
        super(userList, null);
        this.character = characterId;
        this.newRank = newRank;
    }

    @Override
    public MessageType getAction() {
        return MessageType.CHARACTER_PROMOTION;
    }
}
