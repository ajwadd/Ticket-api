package com.eventpro.mapper;

import com.eventpro.dto.response.TicketResponse;
import com.eventpro.entity.Billet;
import com.eventpro.util.QrCodeGenerator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BilletMapper {

    @Mapping(target = "codeUnique", source = "code")
    @Mapping(target = "categorieNom", source = "categorieBillet.nom")
    @Mapping(target = "titreEvenement", source = "evenement.titre")
    @Mapping(target = "qrCodeBase64", source = "code", qualifiedByName = "generateQr")
    TicketResponse toTicketResponse(Billet billet);

    List<TicketResponse> toTicketResponseList(List<Billet> billets);

    @Named("generateQr")
    default String generateQr(String code) {
        if (code == null) return null;
        return QrCodeGenerator.generateQrCodeBase64(code, 250, 250);
    }
}
