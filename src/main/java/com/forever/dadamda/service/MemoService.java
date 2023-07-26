package com.forever.dadamda.service;

import com.forever.dadamda.dto.ErrorCode;
import com.forever.dadamda.dto.scrap.CreateHighlightRequest;
import com.forever.dadamda.dto.scrap.CreateHighlightResponse;
import com.forever.dadamda.dto.scrap.CreateMemoRequest;
import com.forever.dadamda.entity.Memo;
import com.forever.dadamda.entity.scrap.Scrap;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.exception.NotFoundException;
import com.forever.dadamda.repository.MemoRepository;
import com.forever.dadamda.repository.ScrapRepository;
import com.forever.dadamda.service.scrap.ScrapService;
import com.forever.dadamda.service.user.UserService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final UserService userService;
    private final ScrapRepository scrapRepository;
    private final ScrapService scrapService;

    @Transactional
    public CreateHighlightResponse createHighlights(String email,
            CreateHighlightRequest createHighlightRequest) {
        User user = userService.validateUser(email);

        //1. 해당 pageUrl이 DB에 없으면, 스크랩해서 넣는다.
        String pageUrl = createHighlightRequest.getPageUrl();
        Scrap scrap = scrapRepository
                .findByPageUrlAndUserAndDeletedDateIsNull(pageUrl, user)
                .orElseGet(() -> {
                    try {
                        return scrapService.saveScraps(user, pageUrl);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });

        //2. 해당 pageUrl에 해당하는 스크랩에 메모를 저장한다.
        Memo memo = Memo.builder()
                .scrap(scrap)
                .memoText(createHighlightRequest.getSelectedText())
                .memoImageUrl(createHighlightRequest.getSelectedImageUrl())
                .build();

        memoRepository.save(memo);

        return CreateHighlightResponse.of(pageUrl);
    }

    @Transactional
    public void createMemo(String email, CreateMemoRequest createMemoRequest) {
        User user = userService.validateUser(email);

        Scrap scrap = scrapRepository.findByIdAndUserAndDeletedDateIsNull(
                        createMemoRequest.getScrapId(), user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_EXISTS_SCRAP));

        Memo memo = Memo.builder()
                .scrap(scrap)
                .memoText(createMemoRequest.getMemoText())
                .build();

        memoRepository.save(memo);
    }
}
