package de.adesso.iliasdownloader3.xmlentities.exercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static de.adesso.iliasdownloader2.util.Functions.cleanFileName;

//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlAttribute;
//import javax.xml.bind.annotation.XmlElement;

//@XmlAccessorType(XmlAccessType.FIELD)
@Root(name = "File", strict = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XmlExerciseFile {

    @Attribute(name = "size", required = false)
//	@XmlAttribute(name="size")
    private long fileSize;

    @Element(name = "Filename", required = false)
//	@XmlElement(name="Filename")
    private String fileName;

    @Element(name = "Content", required = false)
//	@XmlElement(name="Content")
    private String content;

    public String getFileNameClean() {
        return cleanFileName(fileName);
    }
}
