package com.yuexiang.system.service.impl;

import com.yuexiang.common.exception.InternalServerErrorException;
import com.yuexiang.system.constant.AuthConstants;
import com.yuexiang.system.constant.AuthRedisConstants;
import com.yuexiang.system.constant.CaptchaDrawConstants;
import com.yuexiang.system.domain.vo.CaptchaVO;
import com.yuexiang.system.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;

    // ==================== 安全随机数 ====================
    private static final SecureRandom RANDOM = new SecureRandom();

    // ==================== 预创建字体（避免循环中重复创建） ====================
    private static final Font BASE_FONT = new Font(
            CaptchaDrawConstants.FONT_NAME,
            CaptchaDrawConstants.FONT_STYLE,
            CaptchaDrawConstants.BASE_FONT_SIZE
    );

    public CaptchaServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ======================== 公开接口 ========================

    @Override
    public CaptchaVO generate() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();
        String base64Image = renderToBase64(code);

        cacheCode(captchaId, code);

        return CaptchaVO.builder()
                .captchaId(captchaId)
                .image(CaptchaDrawConstants.BASE64_PREFIX + base64Image)
                .expireSeconds(AuthRedisConstants.CAPTCHA_EXPIRE_SECONDS)
                .build();
    }

    @Override
    public boolean verify(String captchaId, String input) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(input)) {
            return false;
        }

        String key = buildKey(captchaId);
        // 取出后立即删除，保证一次性使用（兼容低版本Redis）
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null) {
            redisTemplate.delete(key);
        }

        return stored != null && stored.equalsIgnoreCase(input.trim());
    }

    // ======================== Redis 操作 ========================

    private void cacheCode(String captchaId, String code) {
        String key = buildKey(captchaId);
        redisTemplate.opsForValue().set(
                key,
                code.toLowerCase(),
                AuthRedisConstants.CAPTCHA_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private String buildKey(String captchaId) {
        return String.format(AuthRedisConstants.CAPTCHA_KEY, captchaId);
    }

    // ======================== 验证码文本生成 ========================

    private String generateCode() {
        StringBuilder sb = new StringBuilder(AuthConstants.CAPTCHA_LENGTH);
        String chars = AuthConstants.CHARS_FOR_CAPTCHA;
        for (int i = 0; i < AuthConstants.CAPTCHA_LENGTH; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ======================== 图片渲染 ========================

    private String renderToBase64(String code) {
        BufferedImage image = new BufferedImage(
                AuthConstants.CAPTCHA_WIDTH,
                AuthConstants.CAPTCHA_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = image.createGraphics();
        try {
            configureGraphics(g);
            drawBackground(g);
            drawCharacters(g, code);
            drawNoiseLines(g);
            drawNoiseCurves(g);
            drawNoiseDots(image);
        } finally {
            g.dispose();
        }

        return encodeToBase64(image);
    }

    /** 开启抗锯齿 */
    private void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }

    /** 白色背景 */
    private void drawBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, AuthConstants.CAPTCHA_WIDTH, AuthConstants.CAPTCHA_HEIGHT);
    }

    /** 绘制字符（带随机旋转） */
    private void drawCharacters(Graphics2D g, String code) {
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomDarkColor());
            g.setFont(BASE_FONT.deriveFont((float) (CaptchaDrawConstants.BASE_FONT_SIZE + RANDOM.nextInt(CaptchaDrawConstants.FONT_SIZE_RANGE))));

            int x = CaptchaDrawConstants.CHAR_START_X + i * CaptchaDrawConstants.CHAR_SPACING;
            int y = CaptchaDrawConstants.CHAR_BASE_Y + RANDOM.nextInt(CaptchaDrawConstants.CHAR_Y_OFFSET);

            // 随机旋转角度，增加识别难度
            double angle = Math.toRadians(RANDOM.nextInt(CaptchaDrawConstants.CHAR_MAX_ROTATION * 2) - CaptchaDrawConstants.CHAR_MAX_ROTATION);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }
    }

    /** 干扰直线 */
    private void drawNoiseLines(Graphics2D g) {
        for (int i = 0; i < CaptchaDrawConstants.NOISE_LINE_COUNT; i++) {
            g.setColor(randomLightColor());
            g.setStroke(new BasicStroke(CaptchaDrawConstants.NOISE_LINE_STROKE_WIDTH));
            g.drawLine(
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT)
            );
        }
    }

    /** 干扰曲线（比直线更难被OCR过滤） */
    private void drawNoiseCurves(Graphics2D g) {
        for (int i = 0; i < CaptchaDrawConstants.NOISE_CURVE_COUNT; i++) {
            g.setColor(randomDarkColor());
            g.setStroke(new BasicStroke(CaptchaDrawConstants.NOISE_CURVE_STROKE_WIDTH));
            QuadCurve2D curve = new QuadCurve2D.Float(
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT)
            );
            g.draw(curve);
        }
    }

    /** 噪点 */
    private void drawNoiseDots(BufferedImage image) {
        for (int i = 0; i < CaptchaDrawConstants.NOISE_DOT_COUNT; i++) {
            image.setRGB(
                    RANDOM.nextInt(AuthConstants.CAPTCHA_WIDTH),
                    RANDOM.nextInt(AuthConstants.CAPTCHA_HEIGHT),
                    randomColor(CaptchaDrawConstants.DOT_COLOR_BOUND).getRGB()
            );
        }
    }

    // ======================== 工具方法 ========================

    private Color randomDarkColor() {
        return randomColor(CaptchaDrawConstants.DARK_COLOR_BOUND);
    }

    private Color randomLightColor() {
        return randomColor(CaptchaDrawConstants.LIGHT_COLOR_BOUND);
    }

    private Color randomColor(int bound) {
        return new Color(RANDOM.nextInt(bound), RANDOM.nextInt(bound), RANDOM.nextInt(bound));
    }

    private String encodeToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(CaptchaDrawConstants.ENCODE_BUFFER_SIZE)) {
            ImageIO.write(image, CaptchaDrawConstants.IMAGE_FORMAT, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("验证码图片编码失败", e);
            throw new InternalServerErrorException("验证码生成失败");
        }
    }
}
