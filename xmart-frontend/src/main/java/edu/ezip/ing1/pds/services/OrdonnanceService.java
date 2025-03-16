package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.business.dto.Ordonnances;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.InsertOrdonnanceClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllOrdonnancesClientRequest;

public class OrdonnanceService {

    private final static String LoggingLabel = "FrontEnd - OrdonnanceService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_ORDONNANCE";
    final String selectRequestOrder = "SELECT_ALL_ORDONNANCE";
    final String deleteRequestOrder = "DELETE_ORDONNANCE";

    private final NetworkConfig networkConfig;
    private int lastInsertedOrdonnanceId = -1;  

    public OrdonnanceService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    
    public void insertOrdonnance(Ordonnance ordonnance, List<String> medicamentsSelectionnes) throws InterruptedException, IOException {
        processOrdonnance(ordonnance, insertRequestOrder, medicamentsSelectionnes);
    }


    public void deleteOrdonnance(Ordonnance ordonnance) throws InterruptedException, IOException {
        processOrdonnance(ordonnance, deleteRequestOrder, null);
    }

    private void processOrdonnance(Ordonnance ordonnance, String requestOrder, List<String> medicamentsSelectionnes) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final StringBuilder descriptionBuilder = new StringBuilder(ordonnance.getDescription());

        if (medicamentsSelectionnes != null && !medicamentsSelectionnes.isEmpty()) {
            descriptionBuilder.append(" - Médicaments: ");
            for (String medicament : medicamentsSelectionnes) {
                descriptionBuilder.append(medicament).append(", ");
            }
            descriptionBuilder.setLength(descriptionBuilder.length() - 2);  
        }

        ordonnance.setDescription(descriptionBuilder.toString());

        final String jsonifiedOrdonnance = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ordonnance);
        logger.trace("Ordonnance JSON : {}", jsonifiedOrdonnance);

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);
        request.setRequestContent(jsonifiedOrdonnance);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        ClientRequest clientRequest = new InsertOrdonnanceClientRequest(networkConfig, 0, request, ordonnance, requestBytes);
        clientRequests.push(clientRequest);

        while (!clientRequests.isEmpty()) {
            final ClientRequest processedRequest = clientRequests.pop();
            processedRequest.join();
            final Ordonnance processedOrdonnance = (Ordonnance) processedRequest.getInfo();
            logger.debug("Thread {} terminé : {} --> {}",
                    processedRequest.getThreadName(),
                    processedOrdonnance.getDescription(),
                    processedRequest.getResult());

            if (requestOrder.equals(insertRequestOrder)) {
                lastInsertedOrdonnanceId = processedOrdonnance.getIdOrdonnance();
            }
        }
    }

    
    public Ordonnances selectOrdonnances() throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();

        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllOrdonnancesClientRequest clientRequest = new SelectAllOrdonnancesClientRequest(
                networkConfig, 0, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} terminé.", joinedClientRequest.getThreadName());
            return (Ordonnances) joinedClientRequest.getResult();
        } else {
            logger.error("Aucune ordonnance trouvée !");
            return null;
        }
    }

    public void insertOrdonnance(Ordonnance ordonnance) throws InterruptedException, IOException {
        processOrdonnance(ordonnance, insertRequestOrder, null);
    }
    
}
