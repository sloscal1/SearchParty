/*
Copyright 2015 Steven Loscalzo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: results.proto

package core.io;

public final class Results {
  private Results() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ResultOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required int64 randseed = 1;
    /**
     * <code>required int64 randseed = 1;</code>
     */
    boolean hasRandseed();
    /**
     * <code>required int64 randseed = 1;</code>
     */
    long getRandseed();

    // required int32 episode = 2;
    /**
     * <code>required int32 episode = 2;</code>
     */
    boolean hasEpisode();
    /**
     * <code>required int32 episode = 2;</code>
     */
    int getEpisode();

    // required double v_error = 3;
    /**
     * <code>required double v_error = 3;</code>
     */
    boolean hasVError();
    /**
     * <code>required double v_error = 3;</code>
     */
    double getVError();

    // repeated double model_theta = 4 [packed = true];
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    java.util.List<java.lang.Double> getModelThetaList();
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    int getModelThetaCount();
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    double getModelTheta(int index);
  }
  /**
   * Protobuf type {@code core.io.Result}
   */
  public static final class Result extends
      com.google.protobuf.GeneratedMessage
      implements ResultOrBuilder {
    // Use Result.newBuilder() to construct.
    private Result(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Result(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Result defaultInstance;
    public static Result getDefaultInstance() {
      return defaultInstance;
    }

    public Result getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private Result(
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
              randseed_ = input.readInt64();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              episode_ = input.readInt32();
              break;
            }
            case 25: {
              bitField0_ |= 0x00000004;
              vError_ = input.readDouble();
              break;
            }
            case 33: {
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                modelTheta_ = new java.util.ArrayList<java.lang.Double>();
                mutable_bitField0_ |= 0x00000008;
              }
              modelTheta_.add(input.readDouble());
              break;
            }
            case 34: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008) && input.getBytesUntilLimit() > 0) {
                modelTheta_ = new java.util.ArrayList<java.lang.Double>();
                mutable_bitField0_ |= 0x00000008;
              }
              while (input.getBytesUntilLimit() > 0) {
                modelTheta_.add(input.readDouble());
              }
              input.popLimit(limit);
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
        if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
          modelTheta_ = java.util.Collections.unmodifiableList(modelTheta_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return core.io.Results.internal_static_core_io_Result_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return core.io.Results.internal_static_core_io_Result_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              core.io.Results.Result.class, core.io.Results.Result.Builder.class);
    }

    public static com.google.protobuf.Parser<Result> PARSER =
        new com.google.protobuf.AbstractParser<Result>() {
      public Result parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Result(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<Result> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required int64 randseed = 1;
    public static final int RANDSEED_FIELD_NUMBER = 1;
    private long randseed_;
    /**
     * <code>required int64 randseed = 1;</code>
     */
    public boolean hasRandseed() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int64 randseed = 1;</code>
     */
    public long getRandseed() {
      return randseed_;
    }

    // required int32 episode = 2;
    public static final int EPISODE_FIELD_NUMBER = 2;
    private int episode_;
    /**
     * <code>required int32 episode = 2;</code>
     */
    public boolean hasEpisode() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required int32 episode = 2;</code>
     */
    public int getEpisode() {
      return episode_;
    }

    // required double v_error = 3;
    public static final int V_ERROR_FIELD_NUMBER = 3;
    private double vError_;
    /**
     * <code>required double v_error = 3;</code>
     */
    public boolean hasVError() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>required double v_error = 3;</code>
     */
    public double getVError() {
      return vError_;
    }

    // repeated double model_theta = 4 [packed = true];
    public static final int MODEL_THETA_FIELD_NUMBER = 4;
    private java.util.List<java.lang.Double> modelTheta_;
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    public java.util.List<java.lang.Double>
        getModelThetaList() {
      return modelTheta_;
    }
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    public int getModelThetaCount() {
      return modelTheta_.size();
    }
    /**
     * <code>repeated double model_theta = 4 [packed = true];</code>
     */
    public double getModelTheta(int index) {
      return modelTheta_.get(index);
    }
    private int modelThetaMemoizedSerializedSize = -1;

    private void initFields() {
      randseed_ = 0L;
      episode_ = 0;
      vError_ = 0D;
      modelTheta_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasRandseed()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasEpisode()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasVError()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt64(1, randseed_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, episode_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeDouble(3, vError_);
      }
      if (getModelThetaList().size() > 0) {
        output.writeRawVarint32(34);
        output.writeRawVarint32(modelThetaMemoizedSerializedSize);
      }
      for (int i = 0; i < modelTheta_.size(); i++) {
        output.writeDoubleNoTag(modelTheta_.get(i));
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
          .computeInt64Size(1, randseed_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, episode_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(3, vError_);
      }
      {
        int dataSize = 0;
        dataSize = 8 * getModelThetaList().size();
        size += dataSize;
        if (!getModelThetaList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        modelThetaMemoizedSerializedSize = dataSize;
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

    public static core.io.Results.Result parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static core.io.Results.Result parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static core.io.Results.Result parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static core.io.Results.Result parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static core.io.Results.Result parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static core.io.Results.Result parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static core.io.Results.Result parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static core.io.Results.Result parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static core.io.Results.Result parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static core.io.Results.Result parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(core.io.Results.Result prototype) {
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
     * Protobuf type {@code core.io.Result}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements core.io.Results.ResultOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return core.io.Results.internal_static_core_io_Result_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return core.io.Results.internal_static_core_io_Result_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                core.io.Results.Result.class, core.io.Results.Result.Builder.class);
      }

      // Construct using core.io.Results.Result.newBuilder()
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
        randseed_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        episode_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        vError_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000004);
        modelTheta_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return core.io.Results.internal_static_core_io_Result_descriptor;
      }

      public core.io.Results.Result getDefaultInstanceForType() {
        return core.io.Results.Result.getDefaultInstance();
      }

      public core.io.Results.Result build() {
        core.io.Results.Result result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public core.io.Results.Result buildPartial() {
        core.io.Results.Result result = new core.io.Results.Result(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.randseed_ = randseed_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.episode_ = episode_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.vError_ = vError_;
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          modelTheta_ = java.util.Collections.unmodifiableList(modelTheta_);
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.modelTheta_ = modelTheta_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof core.io.Results.Result) {
          return mergeFrom((core.io.Results.Result)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(core.io.Results.Result other) {
        if (other == core.io.Results.Result.getDefaultInstance()) return this;
        if (other.hasRandseed()) {
          setRandseed(other.getRandseed());
        }
        if (other.hasEpisode()) {
          setEpisode(other.getEpisode());
        }
        if (other.hasVError()) {
          setVError(other.getVError());
        }
        if (!other.modelTheta_.isEmpty()) {
          if (modelTheta_.isEmpty()) {
            modelTheta_ = other.modelTheta_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureModelThetaIsMutable();
            modelTheta_.addAll(other.modelTheta_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasRandseed()) {
          
          return false;
        }
        if (!hasEpisode()) {
          
          return false;
        }
        if (!hasVError()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        core.io.Results.Result parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (core.io.Results.Result) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required int64 randseed = 1;
      private long randseed_ ;
      /**
       * <code>required int64 randseed = 1;</code>
       */
      public boolean hasRandseed() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required int64 randseed = 1;</code>
       */
      public long getRandseed() {
        return randseed_;
      }
      /**
       * <code>required int64 randseed = 1;</code>
       */
      public Builder setRandseed(long value) {
        bitField0_ |= 0x00000001;
        randseed_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int64 randseed = 1;</code>
       */
      public Builder clearRandseed() {
        bitField0_ = (bitField0_ & ~0x00000001);
        randseed_ = 0L;
        onChanged();
        return this;
      }

      // required int32 episode = 2;
      private int episode_ ;
      /**
       * <code>required int32 episode = 2;</code>
       */
      public boolean hasEpisode() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required int32 episode = 2;</code>
       */
      public int getEpisode() {
        return episode_;
      }
      /**
       * <code>required int32 episode = 2;</code>
       */
      public Builder setEpisode(int value) {
        bitField0_ |= 0x00000002;
        episode_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 episode = 2;</code>
       */
      public Builder clearEpisode() {
        bitField0_ = (bitField0_ & ~0x00000002);
        episode_ = 0;
        onChanged();
        return this;
      }

      // required double v_error = 3;
      private double vError_ ;
      /**
       * <code>required double v_error = 3;</code>
       */
      public boolean hasVError() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>required double v_error = 3;</code>
       */
      public double getVError() {
        return vError_;
      }
      /**
       * <code>required double v_error = 3;</code>
       */
      public Builder setVError(double value) {
        bitField0_ |= 0x00000004;
        vError_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required double v_error = 3;</code>
       */
      public Builder clearVError() {
        bitField0_ = (bitField0_ & ~0x00000004);
        vError_ = 0D;
        onChanged();
        return this;
      }

      // repeated double model_theta = 4 [packed = true];
      private java.util.List<java.lang.Double> modelTheta_ = java.util.Collections.emptyList();
      private void ensureModelThetaIsMutable() {
        if (!((bitField0_ & 0x00000008) == 0x00000008)) {
          modelTheta_ = new java.util.ArrayList<java.lang.Double>(modelTheta_);
          bitField0_ |= 0x00000008;
         }
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public java.util.List<java.lang.Double>
          getModelThetaList() {
        return java.util.Collections.unmodifiableList(modelTheta_);
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public int getModelThetaCount() {
        return modelTheta_.size();
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public double getModelTheta(int index) {
        return modelTheta_.get(index);
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public Builder setModelTheta(
          int index, double value) {
        ensureModelThetaIsMutable();
        modelTheta_.set(index, value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public Builder addModelTheta(double value) {
        ensureModelThetaIsMutable();
        modelTheta_.add(value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public Builder addAllModelTheta(
          java.lang.Iterable<? extends java.lang.Double> values) {
        ensureModelThetaIsMutable();
        super.addAll(values, modelTheta_);
        onChanged();
        return this;
      }
      /**
       * <code>repeated double model_theta = 4 [packed = true];</code>
       */
      public Builder clearModelTheta() {
        modelTheta_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:core.io.Result)
    }

    static {
      defaultInstance = new Result(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:core.io.Result)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_core_io_Result_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_core_io_Result_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rresults.proto\022\007core.io\"U\n\006Result\022\020\n\010ra" +
      "ndseed\030\001 \002(\003\022\017\n\007episode\030\002 \002(\005\022\017\n\007v_error" +
      "\030\003 \002(\001\022\027\n\013model_theta\030\004 \003(\001B\002\020\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_core_io_Result_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_core_io_Result_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_core_io_Result_descriptor,
              new java.lang.String[] { "Randseed", "Episode", "VError", "ModelTheta", });
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
