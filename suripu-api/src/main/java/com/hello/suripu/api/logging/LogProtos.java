// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: log.proto

package com.hello.suripu.api.logging;

public final class LogProtos {
  private LogProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  /**
   * Protobuf enum {@code LogType}
   */
  public enum LogType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>UNSTRUCTURED = 1;</code>
     */
    UNSTRUCTURED(0, 1),
    /**
     * <code>KEY_VALUE = 2;</code>
     */
    KEY_VALUE(1, 2),
    ;

    /**
     * <code>UNSTRUCTURED = 1;</code>
     */
    public static final int UNSTRUCTURED_VALUE = 1;
    /**
     * <code>KEY_VALUE = 2;</code>
     */
    public static final int KEY_VALUE_VALUE = 2;


    public final int getNumber() { return value; }

    public static LogType valueOf(int value) {
      switch (value) {
        case 1: return UNSTRUCTURED;
        case 2: return KEY_VALUE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<LogType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<LogType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<LogType>() {
            public LogType findValueByNumber(int number) {
              return LogType.valueOf(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.hello.suripu.api.logging.LogProtos.getDescriptor().getEnumTypes().get(0);
    }

    private static final LogType[] VALUES = values();

    public static LogType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }

    private final int index;
    private final int value;

    private LogType(int index, int value) {
      this.index = index;
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:LogType)
  }

  public interface sense_logOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // optional int32 unix_time = 1;
    /**
     * <code>optional int32 unix_time = 1;</code>
     */
    boolean hasUnixTime();
    /**
     * <code>optional int32 unix_time = 1;</code>
     */
    int getUnixTime();

    // optional string device_id = 2;
    /**
     * <code>optional string device_id = 2;</code>
     */
    boolean hasDeviceId();
    /**
     * <code>optional string device_id = 2;</code>
     */
    java.lang.String getDeviceId();
    /**
     * <code>optional string device_id = 2;</code>
     */
    com.google.protobuf.ByteString
        getDeviceIdBytes();

    // optional string text = 3;
    /**
     * <code>optional string text = 3;</code>
     */
    boolean hasText();
    /**
     * <code>optional string text = 3;</code>
     */
    java.lang.String getText();
    /**
     * <code>optional string text = 3;</code>
     */
    com.google.protobuf.ByteString
        getTextBytes();

    // optional .LogType property = 4;
    /**
     * <code>optional .LogType property = 4;</code>
     */
    boolean hasProperty();
    /**
     * <code>optional .LogType property = 4;</code>
     */
    com.hello.suripu.api.logging.LogProtos.LogType getProperty();
  }
  /**
   * Protobuf type {@code sense_log}
   */
  public static final class sense_log extends
      com.google.protobuf.GeneratedMessage
      implements sense_logOrBuilder {
    // Use sense_log.newBuilder() to construct.
    private sense_log(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private sense_log(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final sense_log defaultInstance;
    public static sense_log getDefaultInstance() {
      return defaultInstance;
    }

    public sense_log getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private sense_log(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              unixTime_ = input.readInt32();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              deviceId_ = input.readBytes();
              break;
            }
            case 26: {
              bitField0_ |= 0x00000004;
              text_ = input.readBytes();
              break;
            }
            case 32: {
              int rawValue = input.readEnum();
              com.hello.suripu.api.logging.LogProtos.LogType value = com.hello.suripu.api.logging.LogProtos.LogType.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(4, rawValue);
              } else {
                bitField0_ |= 0x00000008;
                property_ = value;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.hello.suripu.api.logging.LogProtos.internal_static_sense_log_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.hello.suripu.api.logging.LogProtos.internal_static_sense_log_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.hello.suripu.api.logging.LogProtos.sense_log.class, com.hello.suripu.api.logging.LogProtos.sense_log.Builder.class);
    }

    public static com.google.protobuf.Parser<sense_log> PARSER =
        new com.google.protobuf.AbstractParser<sense_log>() {
      public sense_log parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new sense_log(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<sense_log> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // optional int32 unix_time = 1;
    public static final int UNIX_TIME_FIELD_NUMBER = 1;
    private int unixTime_;
    /**
     * <code>optional int32 unix_time = 1;</code>
     */
    public boolean hasUnixTime() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional int32 unix_time = 1;</code>
     */
    public int getUnixTime() {
      return unixTime_;
    }

    // optional string device_id = 2;
    public static final int DEVICE_ID_FIELD_NUMBER = 2;
    private java.lang.Object deviceId_;
    /**
     * <code>optional string device_id = 2;</code>
     */
    public boolean hasDeviceId() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional string device_id = 2;</code>
     */
    public java.lang.String getDeviceId() {
      java.lang.Object ref = deviceId_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          deviceId_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string device_id = 2;</code>
     */
    public com.google.protobuf.ByteString
        getDeviceIdBytes() {
      java.lang.Object ref = deviceId_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        deviceId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    // optional string text = 3;
    public static final int TEXT_FIELD_NUMBER = 3;
    private java.lang.Object text_;
    /**
     * <code>optional string text = 3;</code>
     */
    public boolean hasText() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional string text = 3;</code>
     */
    public java.lang.String getText() {
      java.lang.Object ref = text_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          text_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string text = 3;</code>
     */
    public com.google.protobuf.ByteString
        getTextBytes() {
      java.lang.Object ref = text_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        text_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    // optional .LogType property = 4;
    public static final int PROPERTY_FIELD_NUMBER = 4;
    private com.hello.suripu.api.logging.LogProtos.LogType property_;
    /**
     * <code>optional .LogType property = 4;</code>
     */
    public boolean hasProperty() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional .LogType property = 4;</code>
     */
    public com.hello.suripu.api.logging.LogProtos.LogType getProperty() {
      return property_;
    }

    private void initFields() {
      unixTime_ = 0;
      deviceId_ = "";
      text_ = "";
      property_ = com.hello.suripu.api.logging.LogProtos.LogType.UNSTRUCTURED;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, unixTime_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getDeviceIdBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeBytes(3, getTextBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeEnum(4, property_.getNumber());
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, unixTime_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getDeviceIdBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, getTextBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(4, property_.getNumber());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.hello.suripu.api.logging.LogProtos.sense_log parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.hello.suripu.api.logging.LogProtos.sense_log prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code sense_log}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.hello.suripu.api.logging.LogProtos.sense_logOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.hello.suripu.api.logging.LogProtos.internal_static_sense_log_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.hello.suripu.api.logging.LogProtos.internal_static_sense_log_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.hello.suripu.api.logging.LogProtos.sense_log.class, com.hello.suripu.api.logging.LogProtos.sense_log.Builder.class);
      }

      // Construct using com.hello.suripu.api.logging.LogProtos.sense_log.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        unixTime_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        deviceId_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        text_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        property_ = com.hello.suripu.api.logging.LogProtos.LogType.UNSTRUCTURED;
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.hello.suripu.api.logging.LogProtos.internal_static_sense_log_descriptor;
      }

      public com.hello.suripu.api.logging.LogProtos.sense_log getDefaultInstanceForType() {
        return com.hello.suripu.api.logging.LogProtos.sense_log.getDefaultInstance();
      }

      public com.hello.suripu.api.logging.LogProtos.sense_log build() {
        com.hello.suripu.api.logging.LogProtos.sense_log result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.hello.suripu.api.logging.LogProtos.sense_log buildPartial() {
        com.hello.suripu.api.logging.LogProtos.sense_log result = new com.hello.suripu.api.logging.LogProtos.sense_log(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.unixTime_ = unixTime_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.deviceId_ = deviceId_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.text_ = text_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.property_ = property_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.hello.suripu.api.logging.LogProtos.sense_log) {
          return mergeFrom((com.hello.suripu.api.logging.LogProtos.sense_log)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.hello.suripu.api.logging.LogProtos.sense_log other) {
        if (other == com.hello.suripu.api.logging.LogProtos.sense_log.getDefaultInstance()) return this;
        if (other.hasUnixTime()) {
          setUnixTime(other.getUnixTime());
        }
        if (other.hasDeviceId()) {
          bitField0_ |= 0x00000002;
          deviceId_ = other.deviceId_;
          onChanged();
        }
        if (other.hasText()) {
          bitField0_ |= 0x00000004;
          text_ = other.text_;
          onChanged();
        }
        if (other.hasProperty()) {
          setProperty(other.getProperty());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.hello.suripu.api.logging.LogProtos.sense_log parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.hello.suripu.api.logging.LogProtos.sense_log) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // optional int32 unix_time = 1;
      private int unixTime_ ;
      /**
       * <code>optional int32 unix_time = 1;</code>
       */
      public boolean hasUnixTime() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional int32 unix_time = 1;</code>
       */
      public int getUnixTime() {
        return unixTime_;
      }
      /**
       * <code>optional int32 unix_time = 1;</code>
       */
      public Builder setUnixTime(int value) {
        bitField0_ |= 0x00000001;
        unixTime_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int32 unix_time = 1;</code>
       */
      public Builder clearUnixTime() {
        bitField0_ = (bitField0_ & ~0x00000001);
        unixTime_ = 0;
        onChanged();
        return this;
      }

      // optional string device_id = 2;
      private java.lang.Object deviceId_ = "";
      /**
       * <code>optional string device_id = 2;</code>
       */
      public boolean hasDeviceId() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional string device_id = 2;</code>
       */
      public java.lang.String getDeviceId() {
        java.lang.Object ref = deviceId_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          deviceId_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string device_id = 2;</code>
       */
      public com.google.protobuf.ByteString
          getDeviceIdBytes() {
        java.lang.Object ref = deviceId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          deviceId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string device_id = 2;</code>
       */
      public Builder setDeviceId(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        deviceId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string device_id = 2;</code>
       */
      public Builder clearDeviceId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        deviceId_ = getDefaultInstance().getDeviceId();
        onChanged();
        return this;
      }
      /**
       * <code>optional string device_id = 2;</code>
       */
      public Builder setDeviceIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        deviceId_ = value;
        onChanged();
        return this;
      }

      // optional string text = 3;
      private java.lang.Object text_ = "";
      /**
       * <code>optional string text = 3;</code>
       */
      public boolean hasText() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional string text = 3;</code>
       */
      public java.lang.String getText() {
        java.lang.Object ref = text_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          text_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string text = 3;</code>
       */
      public com.google.protobuf.ByteString
          getTextBytes() {
        java.lang.Object ref = text_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          text_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string text = 3;</code>
       */
      public Builder setText(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        text_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string text = 3;</code>
       */
      public Builder clearText() {
        bitField0_ = (bitField0_ & ~0x00000004);
        text_ = getDefaultInstance().getText();
        onChanged();
        return this;
      }
      /**
       * <code>optional string text = 3;</code>
       */
      public Builder setTextBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        text_ = value;
        onChanged();
        return this;
      }

      // optional .LogType property = 4;
      private com.hello.suripu.api.logging.LogProtos.LogType property_ = com.hello.suripu.api.logging.LogProtos.LogType.UNSTRUCTURED;
      /**
       * <code>optional .LogType property = 4;</code>
       */
      public boolean hasProperty() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional .LogType property = 4;</code>
       */
      public com.hello.suripu.api.logging.LogProtos.LogType getProperty() {
        return property_;
      }
      /**
       * <code>optional .LogType property = 4;</code>
       */
      public Builder setProperty(com.hello.suripu.api.logging.LogProtos.LogType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000008;
        property_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional .LogType property = 4;</code>
       */
      public Builder clearProperty() {
        bitField0_ = (bitField0_ & ~0x00000008);
        property_ = com.hello.suripu.api.logging.LogProtos.LogType.UNSTRUCTURED;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:sense_log)
    }

    static {
      defaultInstance = new sense_log(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:sense_log)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_sense_log_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_sense_log_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\tlog.proto\"[\n\tsense_log\022\021\n\tunix_time\030\001 " +
      "\001(\005\022\021\n\tdevice_id\030\002 \001(\t\022\014\n\004text\030\003 \001(\t\022\032\n\010" +
      "property\030\004 \001(\0162\010.LogType**\n\007LogType\022\020\n\014U" +
      "NSTRUCTURED\020\001\022\r\n\tKEY_VALUE\020\002B)\n\034com.hell" +
      "o.suripu.api.loggingB\tLogProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_sense_log_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_sense_log_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_sense_log_descriptor,
              new java.lang.String[] { "UnixTime", "DeviceId", "Text", "Property", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
