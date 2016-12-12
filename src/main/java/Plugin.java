
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.silentsoft.actlist.plugin.ActlistPlugin;
import org.silentsoft.actlist.plugin.messagebox.MessageBox;

import com.jfoenix.controls.JFXCheckBox;


public class Plugin extends ActlistPlugin {
	
	@FXML
	private AnchorPane workTimePane;
	
	@FXML
	private Label lblDateTime;
	
	@FXML
	private Label lblEmpInfo;
	
	@FXML
	private ProgressBar todayProgress;
	
	@FXML
	private Label lblTodayWorkTime;
	
	@FXML
	private ProgressBar weekProgress;
	
	@FXML
	private Label lblWeekWorkTime;
	
	@FXML
	private JFXCheckBox chkCheckIn;
	
	@FXML
	private Button btnCheckIn;
	
	@FXML
	private Label lblCheckIn;
	
	@FXML
	private JFXCheckBox chkCheckOut;
	
	@FXML
	private Button btnCheckOut;
	
	@FXML
	private Label lblCheckOut;
	
	@FXML
	private BorderPane failurePane;
	
	@FXML
	private Label lblFailure;
	
	@FXML
	private BorderPane loadingPane;
	
	private enum Check { IN, OUT };
	
	public static void main(String args[]) throws Exception {}
	
	public Plugin() throws Exception {
		super("SDS Flexwork Plugin");
		
		init();
	}
	
	@Override
	public void applicationActivated() {
		super.applicationActivated();
		
		load();
	}
	
	@Override
	public void pluginActivated() {
		super.pluginActivated();
		
		load();
	}
		
	private void init() {
		// TODO REMOVE URL INFO WHEN BEFORE PUSH INTO GITHUB
		RESTfulAPI.init("http://#secret#/", "#secret#");
	}
	
	@FXML
	private void load() {
		activate(loadingPane);
		
		new Thread(() -> {
			String secureBox = getSecureBox();
			Platform.runLater(() -> {
				if (secureBox == null || "".equals(secureBox)) {
					lblFailure.setText("SSO 정보를 가져오는데 실패했습니다.");
					activate(failurePane);
				} else {
					try {
						initWorkTimePane(secureBox);
						activate(workTimePane);
					} catch (Exception e) {
						lblFailure.setText("일시적인 오류가 발생했습니다.");
						activate(failurePane);
					}
				}
			});
		}).start();
	}
	
	private void initWorkTimePane(String secureBox) throws Exception {
		LocalDateTime localDateTime = LocalDateTime.now();
		lblDateTime.setText(String.format("%04d-%02d-%02d", localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()));
		
		retrieveWorkTimeInfo(secureBox);
	}
	
	private void retrieveWorkTimeInfo(String secureBox) throws Exception {
		Map<String, String> cookieMap = generateCookieMap(getSecureBox());
		// TODO REMOVE URL INFO WHEN BEFORE PUSH INTO GITHUB
		String response = RESTfulAPI.doPost("/#secret#", null, String.class, (worktimeListRequest) -> {
			worktimeListRequest.setHeader("Accept-Language", "ko-KR");
			// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB
			worktimeListRequest.setHeader("Referer", "#secret#");
			worktimeListRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
			worktimeListRequest.setHeader("Cookie", generateCookieStringViaMap(cookieMap));
		}, (worktimeListResponse) -> {
			
		});
		
		Document doc = Jsoup.parse(response);
		String empName = doc.select("table").get(2).select("td").get(1).childNodes().get(0).toString();
		String empNo   = doc.select("table").get(2).select("td").get(7).childNodes().get(0).toString();
		String checkIn = doc.select("table").get(10).select("td").get(5).childNodes().get(0).toString();
		String checkOut = doc.select("table").get(10).select("td").get(7).childNodes().get(0).toString();
		String weekWorkTime = null;
		try {
			weekWorkTime = doc.select("table").get(12).select("tr").get(2).select("td").get(9).childNodes().get(0).toString();
		} catch (Exception e) {
			// 월요일인 경우 누적 근무 시간이 없어서 파싱 에러 발생.
		}
		
		lblEmpInfo.setText(String.format("%s (%s)", empName, empNo));
		
		if (weekWorkTime == null || "".equals(weekWorkTime)) {
			weekProgress.setProgress(0.0);
			lblWeekWorkTime.setText(WorkTime.convertMinuteToString(0));
		} else {
			int[] weekWorkHourAndMinute = WorkTime.parseHourAndMinute(weekWorkTime);
			int weekWorkTimeAsMinute = WorkTime.parseTimeToMinute(weekWorkHourAndMinute[0], weekWorkHourAndMinute[1]);
			weekProgress.setProgress(WorkTime.calcWeekWorkTimeAsPercent(weekWorkTimeAsMinute));
			lblWeekWorkTime.setText(WorkTime.convertMinuteToString(weekWorkTimeAsMinute));
		}
		
		int[] checkInHourAndMinute = WorkTime.parseHourAndMinute(checkIn);
		if (checkInHourAndMinute == null) {
			todayProgress.setProgress(0.0);
			lblTodayWorkTime.setText(WorkTime.convertMinuteToString(0));
			
			lblCheckIn.setText("출근 이력이 없습니다.");
			chkCheckIn.setSelected(false);
			btnCheckIn.setDisable(false);
		} else {
			todayProgress.setProgress(WorkTime.calcTodayWorkTimeAsPercent(checkInHourAndMinute[0], checkInHourAndMinute[1]));
			int todayWorkTimeAsMinute =WorkTime.calcTodayWorkTimeAsMinute(checkInHourAndMinute[0], checkInHourAndMinute[1]);
			lblTodayWorkTime.setText(WorkTime.convertMinuteToString(todayWorkTimeAsMinute));
			
			if (weekWorkTime == null) {
				weekProgress.setProgress(WorkTime.calcWeekWorkTimeAsPercent(todayWorkTimeAsMinute));
				lblWeekWorkTime.setText(WorkTime.convertMinuteToString(todayWorkTimeAsMinute));
			} else {
				int[] weekWorkHourAndMinute = WorkTime.parseHourAndMinute(weekWorkTime);
				int weekAndTodayWorkTimeAsMinute = WorkTime.parseTimeToMinute(weekWorkHourAndMinute[0], weekWorkHourAndMinute[1]) + todayWorkTimeAsMinute;
				weekProgress.setProgress(WorkTime.calcWeekWorkTimeAsPercent(weekAndTodayWorkTimeAsMinute));
				lblWeekWorkTime.setText(WorkTime.convertMinuteToString(weekAndTodayWorkTimeAsMinute));
			}
			
			lblCheckIn.setText(checkIn);
			chkCheckIn.setSelected(true);
			btnCheckIn.setDisable(true);
		}
		
		int[] checkOutHourAndMinute = WorkTime.parseHourAndMinute(checkOut);
		if (checkOutHourAndMinute == null || ((checkIn!=null && checkOut!=null) && (checkIn.equals(checkOut)))) {
			lblCheckOut.setText("퇴근 이력이 없습니다.");
			chkCheckOut.setSelected(false);
		} else {
			lblCheckOut.setText(checkOut);
			chkCheckOut.setSelected(true);
		}
	}
	
	@FXML
	private void checkIn() {
		if (chkCheckIn.isSelected() == false) {
			Optional<ButtonType> result = MessageBox.showConfirm("출근하시겠습니까?");
			result.ifPresent((button) -> {
				if (button == ButtonType.OK) {
					sign(Check.IN);
				}
			});
		}
	}
	
	@FXML
	private void checkOut() {
		if (chkCheckIn.isSelected() == false) {
			MessageBox.showError("출근하기 전에 퇴근하실 수 없습니다.");
			return;
		}
		
		Optional<ButtonType> result = MessageBox.showConfirm("퇴근하시겠습니까?");
		result.ifPresent((button) -> {
			if (button == ButtonType.OK) {
				sign(Check.OUT);
			}
		});
	}
	
	
	private void sign(Check check) {
		activate(loadingPane);
		
		new Thread(() -> {
			boolean isErrorOccur = false;
			try {
				Map<String, String> cookieMap = generateCookieMap(getSecureBox());
				// TODO REMOVE URL INFO WHEN BEFORE PUSH INTO GITHUB
				RESTfulAPI.doPost("/#secret#", null, String.class, (signRequest) -> {
					signRequest.setHeader("Accept-Language", "ko-KR");
					// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB 
					signRequest.setHeader("Referer", "#secret#");
					signRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					signRequest.setHeader("Cookie", generateCookieStringViaMap(cookieMap));
				}, (signResponse) -> {
					
				});
				
				List<NameValuePair> signIn = new ArrayList<NameValuePair>();
				signIn.add(new BasicNameValuePair("mode", (check == Check.IN) ? "" : "checkOut"));
				// TODO REMOVE URL INFO WHEN BEFORE PUSH INTO GITHUB
				RESTfulAPI.doPost("/#secret#", signIn, String.class, (signInRequest) -> {
					signInRequest.setHeader("Accept-Language", "ko-KR");
					// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB 
					signInRequest.setHeader("Referer", "#secret#");
					signInRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					signInRequest.setHeader("Cookie", generateCookieStringViaMap(cookieMap));
				}, (signInResponse) -> {
					
				});
			} catch (Exception e) {
				isErrorOccur = true;
			}
			
			if (isErrorOccur) {
				Platform.runLater(() -> {
					lblFailure.setText("일시적인 오류가 발생했습니다.");
					activate(failurePane);
				});
			} else {
				load();
			}
		}).start();
	}
	
	private void activate(Pane target) {
		if (target == null) {
			return;
		}
		
		for (Pane pane : Arrays.asList(workTimePane, failurePane, loadingPane)) {
			if (pane == target) {
				pane.setVisible(true);
			} else {
				pane.setVisible(false);
			}
		}
	}
	
	private Map<String, String> generateCookieMap(String secureBox) throws Exception {
		HashMap<String, String> cookieMap = new HashMap<String, String>();
		
		// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB
		RESTfulAPI.doGet("/#secret#", String.class, (indexRequest) -> {
			indexRequest.setHeader("Accept", "text/html, application/xhtml+xml, */*");
			indexRequest.setHeader("Accept-Language", "ko-KR");
			indexRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
			indexRequest.setHeader("Accept-Encoding", "gzip, deflate");
		}, (indexResponse) -> {
			setCookieToMap(cookieMap, indexResponse.getHeaders("Set-cookie"));
		});
		
		List<NameValuePair> indexUrl = new ArrayList<NameValuePair>();
		indexUrl.add(new BasicNameValuePair("SSO_TRAY_DATA", secureBox));
		indexUrl.add(new BasicNameValuePair("initSession", "true"));
		// TODO REMOVE FROM REFER AND TEMP URL INFO WHEN BEFORE PUSH INTO GITHUB
		indexUrl.add(new BasicNameValuePair("fromRefer", "#secret#"));
		indexUrl.add(new BasicNameValuePair("temp_url", "#secret#"));
		indexUrl.add(new BasicNameValuePair("auth_dept", "1"));
		indexUrl.add(new BasicNameValuePair("redirectURI", ""));
		// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB
		RESTfulAPI.doPost("/#secret#", indexUrl, String.class, (indexUrlRequest) -> {
			indexUrlRequest.setHeader("Accept-Language", "ko-KR");
			// TODO REMOVE REFER INFO WHEN BEFORE PUSH INTO GITHUB
			indexUrlRequest.setHeader("Referer", "#secret#");
			indexUrlRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
			indexUrlRequest.setHeader("Cookie", generateCookieStringViaMap(cookieMap));
		}, (indexUrlResponse) -> {
			setCookieToMap(cookieMap, indexUrlResponse.getHeaders("Set-cookie"));
		});
		
		return cookieMap;
	}
	
	private String generateCookieStringViaMap(Map<String, String> map) {
		ArrayList<String> list = new ArrayList<String>();
		
		for (Entry<String, String> entry : map.entrySet()) {
			list.add(String.join("=", entry.getKey(), entry.getValue()));
		}
		
		return String.join("; ", list);
	}
	
	private void setCookieToMap(Map<String, String> map, Header[] headers) {
		for (Header header : headers) {
			String[] cookie = header.getValue().split(";")[0].split("=");
			map.put(cookie[0], cookie[1]);
		}
	}
	
	// TODO : TEMP CODING HERE. I DO NOT HAVE ANY SOURCE CODE NOW.
	boolean initFlag = false;
	private String getSecureBox() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String value = (initFlag ? "SSO" : "");
		initFlag = true;
		
		return value;
	}
	
}
