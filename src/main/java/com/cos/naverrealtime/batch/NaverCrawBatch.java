package com.cos.naverrealtime.batch;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cos.naverrealtime.domain.NaverNews;
import com.cos.naverrealtime.domain.NaverNewsRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;


@RequiredArgsConstructor
@Component
public class NaverCrawBatch {
	
	private long aid = 278100;
	private final NaverNewsRepository naverNewsRepository;
	
	// 초 분 시 일 월 주
	// @Scheduled(cron = "* * 1 * * *", zone = "Asia/Seoul")
	
	@Scheduled(cron = "0 11 12 * * *", zone = "Asia/Seoul")
	public void navernewscrawling() {
		System.out.println("배치 프로그램 시작===============================");
		List<NaverNews> naverNewsList = new ArrayList<>();
		int successCount = 0;
		int errorCount = 0;
		int crawCount = 0;
		while (true) {
			String aidStr = String.format("%010d", aid);
			System.out.println("aidStr : " + aidStr);
			String url = "https://news.naver.com/main/read.naver?mode=LSD&mid=shm&sid1=103&oid=437&aid=" + aidStr;

			try {
				Document doc = Jsoup.connect(url).get();
				// title, company, createdAt
				String title = doc.selectFirst("#articleTitle").text();
				String company = doc.selectFirst(".press_logo img").attr("alt");
				String createdAt = doc.selectFirst(".t11").text();

				// System.out.println("title : " + title);
				// System.out.println("company : " + company);
				// System.out.println("createdAt : " + createdAt);

				LocalDate today = LocalDate.now();

				LocalDate yesterday = LocalDate.now().minusDays(1);
				// System.out.println(yesterday);

				createdAt = createdAt.substring(0, 10);
				createdAt = createdAt.replace(".", "-");
				// System.out.println(createdAt);

				if (today.toString().equals(createdAt)) {
					System.out.println("createdAt : " + createdAt);
					System.out.println("배치 프로그램 종료");

					break; // while문 빠져나가고 중지
				}

				if (yesterday.toString().equals(createdAt)) { // List 컬렉션에 모았다가 DB에 save() 하기
					System.out.println("어제의 기사입니다. 크롤링 완료");

					naverNewsList.add(NaverNews.builder().title(title).company(company)
							.createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1))).build());
					crawCount++;
				} // end of if

				successCount++;
			} catch (Exception e) {
				System.out.println("해당 주소에 페이지를 찾을 수 없습니다 : " + e.getMessage());
				errorCount++;
			} // end of try / catch
			aid++;

		} // end of While
		System.out.println("배치 프로그램 종료");
		System.out.println("successCount : " + successCount);
		System.out.println("errorCount : " + errorCount);
		System.out.println("crawCount : " + crawCount);
		System.out.println("마지막 aid 값 :" + aid);
		System.out.println("컬렉션에 담은 크기 :" + naverNewsList.size());
		// naverNewsRepository.saveAll(naverNewsList);
		
		Flux.fromIterable(naverNewsList)
		                 .flatMap(naverNewsRepository::save)
		                 .subscribe();
		
    }
}
	