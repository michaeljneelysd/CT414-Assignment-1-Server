package server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import ct414.UnauthorizedAccess;

public class LogonServer {
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private HashMap<String, String> accessDetails;
	
	public LogonServer(String db) {
		this.accessDetails = new HashMap<String,String>();
		this.loadRegistry(db);
	}
	
	private void loadRegistry(String dbName) {
		ArrayList<String> lines = Utils.loadLines(dbName);
		lines.forEach(line -> {
			String[] data = line.split(";");
			this.accessDetails.put(data[0], data[1]);
		});
	}

	protected String login(String id, String pwd) throws UnauthorizedAccess {
		if (this.accessDetails.get(id).equals(pwd)) {
			return this.generateToken(id, LocalDateTime.of(LocalDate.now(), LocalTime.now().plusMinutes(30)));
		} else {
			throw new UnauthorizedAccess("Incorrect Logon Details");
		}
	}
	
	protected String generateToken(String id, LocalDateTime expiryDate) {
		return this.encode("" + id + "AAA" + expiryDate.format(formatter) + "");
	}
	
	protected boolean isTokenValid(String token) {
		LocalDateTime expiryDate = LocalDateTime.parse(this.decode(token).split("AAA")[1], formatter);
		return (LocalDateTime.now().isBefore(expiryDate));
	}

	protected String getStudentIDFromToken(String token){
		return this.decode(token).split("AAA")[0];
	}

	private String encode(String token) {
		return Base64.getEncoder().encodeToString(token.getBytes());
	}
	
	private String decode(String encrypted_token) {
		return new String(Base64.getDecoder().decode(encrypted_token.getBytes()));
	}
}
