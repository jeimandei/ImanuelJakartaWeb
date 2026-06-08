package com.jeimandei.imanuelbytes.interaction.service;

import com.jeimandei.imanuelbytes.interaction.dto.ContactMessageDto;
import com.jeimandei.imanuelbytes.interaction.dto.CreateContactMessageRequest;
import com.jeimandei.imanuelbytes.interaction.dto.UpdateContactStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactMessageService {

    ContactMessageDto submitContactMessage(CreateContactMessageRequest request);

    Page<ContactMessageDto> getAllMessages(Pageable pageable);

    ContactMessageDto getMessageById(Long id);

    ContactMessageDto updateStatus(Long id, UpdateContactStatusRequest request);

    long getUnreadCount();
}
