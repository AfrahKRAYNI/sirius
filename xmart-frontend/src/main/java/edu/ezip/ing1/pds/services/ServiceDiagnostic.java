package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeleteSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.InsertSymptomeClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllSymptomesClientRequest;

public class ServiceDiagnostic {

    private final static String LoggingLabel = "FrontEnd - ServiceDiagnostic";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_SYMPTOME";
    final String selectRequestOrder = "SELECT_ALL_SYMPTOMES";
    final String deleteRequestOrder = "DELETE_SYMPTOME";

    private final NetworkConfig networkConfig;

    public ServiceDiagnostic(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertSymptome(Symptomes symptome) throws InterruptedException, IOException {
        processSymptome(symptome, insertRequestOrder);
    }

    public void deleteSymptome(Symptomes symptome) throws InterruptedException, IOException {
        processSymptome(symptome, deleteRequestOrder);
    }

    private void processSymptome(Symptomes symptome, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedSymptome = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(symptome);
        logger.trace("Symptome JSON : {}", jsonifiedSymptome);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedSymptome);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest symptomeRequest = null;

        if (requestOrder.equals(insertRequestOrder)) {
            symptomeRequest = new InsertSymptomeClientRequest(networkConfig, 0, request, symptome, requestBytes);
        } else if (requestOrder.equals(deleteRequestOrder)) {
            symptomeRequest = new DeleteSymptomeClientRequest(networkConfig, 0, request, symptome, requestBytes);
        }

        symptomeRequests.push(symptomeRequest);

        while (!symptomeRequests.isEmpty()) {
            final ClientRequest processedRequest = symptomeRequests.pop();
            processedRequest.join();
            logger.debug("Thread {} terminé pour symptôme {} --> {}",
                    processedRequest.getThreadName(),
                    symptome.getNom(), processedRequest.getResult());
        }
    }

    public Symptomes selectSymptomes() throws InterruptedException, IOException {
        final Deque<ClientRequest> symptomeRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllSymptomesClientRequest symptomeRequest = new SelectAllSymptomesClientRequest(
                networkConfig, 0, request, null, requestBytes);
        symptomeRequests.push(symptomeRequest);

        if (!symptomeRequests.isEmpty()) {
            final ClientRequest joinedSymptomeRequest = symptomeRequests.pop();
            joinedSymptomeRequest.join();
            logger.debug("Thread {} terminé.", joinedSymptomeRequest.getThreadName());
            return (Symptomes) joinedSymptomeRequest.getResult();
        } else {
            logger.error("Aucun symptôme trouvé !");
            return null;
        }
    }
}
