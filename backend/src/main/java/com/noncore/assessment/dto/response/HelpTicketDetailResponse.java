package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.entity.HelpTicketMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelpTicketDetailResponse {
    private HelpTicket ticket;
    private List<HelpTicketMessage> messages;
}
