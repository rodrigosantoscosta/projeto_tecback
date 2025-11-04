package br.com.oficina.oficina.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class FeriadoNacionalResponse {
    private String date;
    private String name;
    private String type;

    public FeriadoNacionalResponse() {
    }

    public FeriadoNacionalResponse(String date, String name, String type) {
        this.date = date;
        this.name = name;
        this.type = type;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}


