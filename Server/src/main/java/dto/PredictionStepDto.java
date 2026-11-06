package dto;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object (DTO) che descrive lo stato del nodo raggiunto durante
 * la navigazione guidata stateless nell'albero di regressione.
 *
 * Se il nodo è una foglia ({@code isLeaf == true}), {@code predictedValue} contiene
 * il valore continuo calcolato come media aritmetica, mentre {@code attributeName}
 * e {@code branchOptions} sono nulli o vuoti.
 * Se il nodo è interno ({@code isLeaf == false}), {@code attributeName} indica
 * l'attributo discriminante e {@code branchOptions} contiene le condizioni dei rami figli.
 *
 * @param isLeaf         {@code true} se il nodo raggiunto è una foglia, {@code false} se è uno split
 * @param predictedValue valore continuo predetto dalla foglia, oppure {@code null} per nodi interni
 * @param attributeName  nome dell'attributo su cui effettuare lo split, oppure {@code null} per foglie
 * @param branchOptions  lista delle condizioni testuali dei rami figli (es. {@code "=A"}, {@code "<=20.5"})
 */
public record PredictionStepDto(
        boolean isLeaf,
        Double predictedValue,
        String attributeName,
        List<String> branchOptions
) implements Serializable {
}
