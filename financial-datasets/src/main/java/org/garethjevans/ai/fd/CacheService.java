package org.garethjevans.ai.fd;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

  private final File cacheDir;

  public CacheService(File cacheDir) {
    this.cacheDir = cacheDir;
  }

  public boolean keyExists(String cacheKey) {
    String hash = hash(cacheKey);
    return new File(cacheDir, hash).exists();
  }

  public String get(String cacheKey) {
    String hash = hash(cacheKey);

    Path path = Paths.get(cacheDir.getPath(), hash);
    try {
      return Files.readAllLines(path).get(0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void save(String cacheKey, String response) {
    String hash = hash(cacheKey);
    LOGGER.info("Saving {} ({})\n{}", cacheKey, hash, response);

    Path path = Paths.get(cacheDir.getPath(), hash);
    byte[] strToBytes = response.getBytes();

    try {
      Files.write(path, strToBytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String hash(String cacheKey) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    md.update(cacheKey.getBytes());
    byte[] digest = md.digest();
    BigInteger no = new BigInteger(1, digest);
    return String.format("%032x", no).toUpperCase();
  }
}
