package com.dummy.security.JWTToken.Controller;

import java.security.Key;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;


@Controller
public class TokenController {
	
	// Key is hardcoded here for simplicity. 
		// Ideally this will get loaded from env configuration/secret vault
	@Value("${file.secretKey}")
	private String secret;
	
	@GetMapping("/GenerateToken")
	public ResponseEntity<String> generateToken() {
		ResponseEntity<String> response = null;

 		// ADD basic auth later
 	
 		ObjectMapper responseJSON = new ObjectMapper();
 		 // Enable pretty printing
 		responseJSON.enable(SerializationFeature.INDENT_OUTPUT);
 		
 		
 		Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secret), 
 		                            SignatureAlgorithm.HS256.getJcaName());

 		Instant now = Instant.now();
 		String jwtToken = Jwts.builder()
 		        .claim("userName", "ADMIN BBBBB")
 		        .claim("basicAuthUserName", "User")
 		       .claim("basicAuthPassword", "User123")
 		        .setSubject("tokenGenerator")
 		        .setId(UUID.randomUUID().toString())
 		        .setIssuedAt(Date.from(now))
 		        .setExpiration(Date.from(now.plus(2l, ChronoUnit.MINUTES)))
 		        .signWith(hmacKey)
 		        .compact();
 		
 		ObjectNode jsonObject = responseJSON.createObjectNode();
 		
			jsonObject.put("access_token", jwtToken);
			jsonObject.put("status", "success");
			jsonObject.put("expiationTimeLimit", "Two minutes");
			Date date = new Date();
			  Timestamp timestamp2 = new Timestamp(date.getTime());
			jsonObject.put("issued at", timestamp2.toString());
	
		
			  String jsonResponse;
			    try {
			        jsonResponse = responseJSON.writeValueAsString(jsonObject);
			    } catch (JsonProcessingException e) {
			        // Handle the exception appropriately
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			    }
			    
 		response = new ResponseEntity(jsonResponse, HttpStatus.OK);
 		return response;
 	//return ResponseEntity.ok(jsonResponse);
		
	}
	
	
	@PostMapping("/ValidateToken")
	public ResponseEntity<String> validateJWTToken(@RequestBody String request) throws JsonMappingException, JsonProcessingException{
		  String jsonResponse = null;
		ResponseEntity<String> response = null;

		// ADD basic auth later
	
		// basically validateToken API will not be there but, direct method call will be there in all the other
		//resource APIs to validate the received token along with the sent userName
		//Validate token API will be needed if App login page is needed to add with O-Auth.so 
		// the PTM app can hit this war and generate/validate  token.
		// request token is not required since new call is to made again.
		String jwtString = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		ObjectMapper responseMapper = null;

		JsonNode requetJSON = objMapper.readTree(request);
		 ObjectNode responseObjNode = null;
		//if(EkaStringUtil.isNotEmpty(request)){
			//JSONObject jsonValue = JSONObject.fromObject(request);
			jwtString = requetJSON.get("access_token").asText();
		//}
		
		 String secret = "asdfSFS34wfsdfsdfSDSD32dfsddDDerQSNCK34SOWEK5354fdgdf4";
		    Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secret), 
		                                    SignatureAlgorithm.HS256.getJcaName());
		    Jws<Claims> jwt = null;
try {
		   jwt = Jwts.parserBuilder()
		            .setSigningKey(hmacKey)
		            .build()
		            .parseClaimsJws(jwtString);
}
		catch(SignatureException se) {
			se.printStackTrace();
			
			responseMapper = new ObjectMapper();
			 responseObjNode =  responseMapper.createObjectNode();
			
			responseObjNode.removeAll();
			responseObjNode.put("status", "Failed");
			responseObjNode.put("Description", "Token Signature is tampered. Request blocked");

			    try {
			        jsonResponse = responseMapper.writeValueAsString(responseObjNode);
			    } catch (JsonProcessingException e) {
			        // Handle the exception appropriately
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			    }
			    
			 response = new ResponseEntity(jsonResponse, HttpStatus.OK);
				//logger.info("JWT string parsed to json " +jwt);
				return response;
				
		}catch (ExpiredJwtException expiredException) {
			
			expiredException.printStackTrace();
			responseMapper = new ObjectMapper();
			 responseObjNode =  responseMapper.createObjectNode();
			
			responseObjNode.removeAll();
			responseObjNode.put("status", "Failed");
			responseObjNode.put("Description", "Token is expired. Request another token");
			 
			  try {
			        jsonResponse = responseMapper.writeValueAsString(responseObjNode);
			    } catch (JsonProcessingException e) {
			        // Handle the exception appropriately
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			    }
			    
			 
			 response = new ResponseEntity(jsonResponse, HttpStatus.OK);
				//logger.info("JWT string parsed to json " +jwt);
				return response;
		}
		 

responseMapper = new ObjectMapper();
 responseObjNode =  responseMapper.createObjectNode();
 responseObjNode.removeAll();
responseObjNode.put("status", "Success");
responseObjNode.put("Description", "Token Validation Successful.");
 
  try {
        jsonResponse = responseMapper.writeValueAsString(responseObjNode);
    } catch (JsonProcessingException e) {
        // Handle the exception appropriately
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
 
 response = new ResponseEntity(jsonResponse, HttpStatus.OK);
	//logger.info("JWT string parsed to json " +jwt);
	return response;
	
	}

}
