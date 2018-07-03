/**
 * 
 */
package cn.pdd.util.json;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cn.pdd.util.date.DateHelper;

/**
 * gson 辅助类;
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class GsonHelper {

	
	public static Gson create(){
		return create(false);
	}

	public static Gson create(boolean excludeFieldsWithoutExposeAnnotation){
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(java.util.Date.class, new Date1TypeAdapter())
		.registerTypeAdapter(java.sql.Date.class, new Date2TypeAdapter())
		.registerTypeAdapter(java.sql.Timestamp.class, new Date3TypeAdapter())
		.registerTypeAdapter(BigDecimal.class, new BigDecimalDeserializer());
		if(excludeFieldsWithoutExposeAnnotation){
			gb.excludeFieldsWithoutExposeAnnotation();
		}
		return gb.create();
	}
	
	static class Date1TypeAdapter implements JsonSerializer<java.util.Date>, JsonDeserializer<java.util.Date>{

		/* (non-Javadoc)
		 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
		 */
		public java.util.Date deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			String s = json.getAsString();
			java.util.Date result = DateHelper.parseDate(s);
			return result;
		}

		/* (non-Javadoc)
		 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
		 */
		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			if(src != null){
				return new JsonPrimitive(DateHelper.format(src, DateHelper.DATE_FMT_DEFAULT));
			}
			return null;
		}
		
	}
	
	static class Date2TypeAdapter implements JsonSerializer<java.sql.Date>, JsonDeserializer<java.sql.Date>{

		/* (non-Javadoc)
		 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
		 */
		public java.sql.Date deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			String s = json.getAsString();
			java.util.Date tmp = DateHelper.parseDate(s);
			java.sql.Date result = null;
			if(tmp != null)
				result = new java.sql.Date(tmp.getTime());
			return result;
		}

		/* (non-Javadoc)
		 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
		 */
		@Override
		public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
			if(src != null){
				return new JsonPrimitive(DateHelper.format(src, DateHelper.DATE_FMT_DEFAULT));
			}
			return null;
		}
	}
	
	static class Date3TypeAdapter implements JsonSerializer<java.sql.Timestamp>, JsonDeserializer<java.sql.Timestamp>{

		/* (non-Javadoc)
		 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
		 */
		public java.sql.Timestamp deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			String s = json.getAsString();
			java.util.Date tmp = DateHelper.parseDate(s);
			java.sql.Timestamp result = null;
			if(tmp != null)
				result = new java.sql.Timestamp(tmp.getTime());
			return result;
		}

		/* (non-Javadoc)
		 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
		 */
		@Override
		public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
			if(src != null){
				return new JsonPrimitive(DateHelper.format(src, DateHelper.DATE_FMT_DEFAULT));
			}
			return null;
		}
		
	}
	
	static class BigDecimalDeserializer implements JsonDeserializer<BigDecimal>{

		public BigDecimal deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
				throws JsonParseException {
			String s = json.getAsString();
			if("".equals(s)
					||"null".equals(s))
				return null;
			return new BigDecimal(s);
		}
		
	}
}
