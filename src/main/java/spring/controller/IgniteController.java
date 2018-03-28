package spring.controller;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.transactions.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IgniteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteController.class);

    @Autowired
    IgniteCache<String, String> cache;

    @GetMapping(value = "/cache", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllCacheData() {
        Map<Object, Object> data = new HashMap<>();
        LOGGER.info("Cache reading Start");
        try (QueryCursor cursor = cache.query(new ScanQuery((k, p) -> true))) {
            for (Object p : cursor) {
                IgniteBiTuple biTuple = (IgniteBiTuple) p;
                data.put(biTuple.getKey(), biTuple.getValue());
            }
        }
        LOGGER.info("Cache reading End");
        if (data.isEmpty()) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<Object>(data, HttpStatus.OK);
        }
    }

    @PutMapping(value = "/cache", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> putCacheData(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value) {
        Map<Object, Object> data = new HashMap<>();
        String errMsg = "";
        try {
            cache.put(key,value);
            LOGGER.info("Data inserted in cache successfully.");
        } catch (TransactionException e) {
            errMsg = e.getMessage();
            LOGGER.error("Data insertion failed. ",e.getMessage());
        }

        if (errMsg!="") {
            data.put("msg", errMsg);
            return new ResponseEntity<Object>(data,HttpStatus.NOT_MODIFIED);
        } else {
            data.put("msg", "Data inserted in cache successfully.");
            return new ResponseEntity<Object>(data, HttpStatus.OK);
        }
    }
}