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
import edu.ezip.ing1.pds.business.dto.Maladie;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeleteMaladieClientRequest;
import edu.ezip.ing1.pds.requests.InsertMaladieClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllMaladiesClientRequest;

public class ServiceMaladie {

    private final static String LoggingLabel = "FrontEnd - ServiceMaladie";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_MALADIE";
    final String selectRequestOrder = "SELECT_ALL_MALADIES";
    final String deleteRequestOrder = "DELETE_MALADIE";

    private final NetworkConfig networkConfig;

    public ServiceMaladie(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertMaladie(Maladie maladie) throws InterruptedException, IOException {
        processMaladie(maladie, insertRequestOrder);
    }

    public void deleteMaladie(Maladie maladie) throws InterruptedException, IOException {
        processMaladie(maladie, deleteRequestOrder);
    }

    private void processMaladie(Maladie maladie, String requestOrder) throws InterruptedException, IOException {
        final Deque<ClientRequest> maladieRequests = new ArrayDeque<>();

        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedMaladie = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(maladie);
        logger.trace("Maladie en JSON : {}", jsonifiedMaladie);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedMaladie);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest maladieRequest = null;

        if (requestOrder.equals(insertRequestOrder)) {
            maladieRequest = new InsertMaladieClientRequest(networkConfig, 0, request, maladie, requestBytes);
        } else if (requestOrder.equals(deleteRequestOrder)) {
            maladieRequest = new DeleteMaladieClientRequest(networkConfig, 0, request, maladie, requestBytes);
        }

        maladieRequests.push(maladieRequest);

        while (!maladieRequests.isEmpty()) {
            final ClientRequest processedRequest = maladieRequests.pop();
            processedRequest.join();
            logger.debug("Thread {} terminé : Maladie {} --> {}",
                    processedRequest.getThreadName(),
                    maladie.getNom(), processedRequest.getResult());
        }
    }

    public Maladie selectMaladies() throws InterruptedException, IOException {
        final Deque<ClientRequest> maladieRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllMaladiesClientRequest maladieRequest = new SelectAllMaladiesClientRequest(
                networkConfig, 0, request, null, requestBytes);
        maladieRequests.push(maladieRequest);

        if (!maladieRequests.isEmpty()) {
            final ClientRequest joinedMaladieRequest = maladieRequests.pop();
            joinedMaladieRequest.join();
            logger.debug("Thread {} terminé.", joinedMaladieRequest.getThreadName());
            return (Maladie) joinedMaladieRequest.getResult();
        } else {
            logger.error("Aucune maladie trouvée !");
            return null;
        }
    }
}
