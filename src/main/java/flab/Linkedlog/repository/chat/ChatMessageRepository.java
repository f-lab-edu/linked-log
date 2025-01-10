package flab.Linkedlog.repository.chat;

import flab.Linkedlog.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    
}
