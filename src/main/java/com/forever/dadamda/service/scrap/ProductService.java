package com.forever.dadamda.service.scrap;

import com.forever.dadamda.dto.ErrorCode;
import com.forever.dadamda.dto.scrap.UpdateScrapRequest;
import com.forever.dadamda.entity.scrap.Product;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.exception.NotFoundException;
import com.forever.dadamda.repository.ProductRepository;
import com.forever.dadamda.service.user.UserService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public Product saveProduct(JSONObject crawlingResponse, User user, String pageUrl) {
        Product product = Product.builder().user(user).pageUrl(pageUrl)
                .title(crawlingResponse.get("title").toString())
                .thumbnailUrl(crawlingResponse.get("thumbnail_url").toString())
                .price(crawlingResponse.get("price").toString())
                .siteName(crawlingResponse.get("site_name").toString()).build();

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(User user, UpdateScrapRequest updateScrapRequest) {
        Product product = productRepository.findByIdAndUserAndDeletedDateIsNull(
                        updateScrapRequest.getScrapId(), user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_EXISTS_SCRAP));
        product.update(updateScrapRequest.getTitle(), updateScrapRequest.getDescription(),
                updateScrapRequest.getSiteName());
        product.updateProduct(updateScrapRequest.getPrice());
        return product;
    }

    @Transactional
    public Long getProductCount(String email) {
        User user = userService.validateUser(email);
        return productRepository.countByUserAndDeletedDateIsNull(user);
    }
}
