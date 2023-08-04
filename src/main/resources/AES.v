// Generator : SpinalHDL v1.6.2    git head : 685405804ac0fa51f884fe0ee6813ba6f1f31e4e
// Component : AES
// Git hash  : 685405804ac0fa51f884fe0ee6813ba6f1f31e4e

`timescale 1ns/1ps 

module AES (
  input               io_clk,
  input               io_reset,
  input               io_enable,
  input               io_plaintext_valid,
  output reg          io_plaintext_ready,
  input      [31:0]   io_plaintext_payload,
  input               io_key_valid,
  output reg          io_key_ready,
  input      [31:0]   io_key_payload,
  output reg          io_ciphertext_valid,
  input               io_ciphertext_ready,
  output     [31:0]   io_ciphertext_payload,
  output reg          io_done
);
  localparam States2_sIdle = 2'd0;
  localparam States2_sInit = 2'd1;
  localparam States2_sRound = 2'd2;
  localparam States2_sDone = 2'd3;

  reg        [7:0]    r_sbox_io_subBytesInput;
  wire       [7:0]    r_addroundkey_io_b;
  wire       [7:0]    r_sbox_io_subBytesOutput;
  wire       [7:0]    r_mixcolumns_io_mcOut1;
  wire       [7:0]    r_mixcolumns_io_mcOut2;
  wire       [7:0]    r_mixcolumns_io_mcOut3;
  wire       [7:0]    r_mixcolumns_io_mcOut4;
  wire       [3:0]    _zz_r_round_valueNext;
  wire       [0:0]    _zz_r_round_valueNext_1;
  wire       [4:0]    _zz_r_control_valueNext;
  wire       [0:0]    _zz_r_control_valueNext_1;
  reg        [7:0]    _zz_io_ciphertext_payload;
  wire       [1:0]    _zz_io_ciphertext_payload_1;
  reg        [7:0]    _zz_io_ciphertext_payload_2;
  wire       [1:0]    _zz_io_ciphertext_payload_3;
  reg        [7:0]    _zz_io_ciphertext_payload_4;
  wire       [1:0]    _zz_io_ciphertext_payload_5;
  reg        [7:0]    _zz_io_ciphertext_payload_6;
  wire       [1:0]    _zz_io_ciphertext_payload_7;
  wire       [7:0]    _zz_r_roundConstant;
  reg        [7:0]    r_stateReg_0_0;
  reg        [7:0]    r_stateReg_0_1;
  reg        [7:0]    r_stateReg_0_2;
  reg        [7:0]    r_stateReg_0_3;
  reg        [7:0]    r_stateReg_1_0;
  reg        [7:0]    r_stateReg_1_1;
  reg        [7:0]    r_stateReg_1_2;
  reg        [7:0]    r_stateReg_1_3;
  reg        [7:0]    r_stateReg_2_0;
  reg        [7:0]    r_stateReg_2_1;
  reg        [7:0]    r_stateReg_2_2;
  reg        [7:0]    r_stateReg_2_3;
  reg        [7:0]    r_stateReg_3_0;
  reg        [7:0]    r_stateReg_3_1;
  reg        [7:0]    r_stateReg_3_2;
  reg        [7:0]    r_stateReg_3_3;
  reg        [7:0]    r_keyReg_0_0;
  reg        [7:0]    r_keyReg_0_1;
  reg        [7:0]    r_keyReg_0_2;
  reg        [7:0]    r_keyReg_0_3;
  reg        [7:0]    r_keyReg_1_0;
  reg        [7:0]    r_keyReg_1_1;
  reg        [7:0]    r_keyReg_1_2;
  reg        [7:0]    r_keyReg_1_3;
  reg        [7:0]    r_keyReg_2_0;
  reg        [7:0]    r_keyReg_2_1;
  reg        [7:0]    r_keyReg_2_2;
  reg        [7:0]    r_keyReg_2_3;
  reg        [7:0]    r_keyReg_3_0;
  reg        [7:0]    r_keyReg_3_1;
  reg        [7:0]    r_keyReg_3_2;
  reg        [7:0]    r_keyReg_3_3;
  reg        [1:0]    r_aesState;
  reg                 r_round_willIncrement;
  reg                 r_round_willClear;
  reg        [3:0]    r_round_valueNext;
  reg        [3:0]    r_round_value;
  wire                r_round_willOverflowIfInc;
  wire                r_round_willOverflow;
  reg                 r_control_willIncrement;
  reg                 r_control_willClear;
  reg        [4:0]    r_control_valueNext;
  reg        [4:0]    r_control_value;
  wire                r_control_willOverflowIfInc;
  wire                r_control_willOverflow;
  reg        [7:0]    r_roundConstant;
  reg        [7:0]    r_rc;
  wire                r_finalKeyAdd;
  wire                r_finalRound;
  wire                when_AES_l44;
  wire                when_AES_l56;
  wire                when_AES_l62;
  wire                when_AES_l80;
  wire                when_AES_l86;
  wire                when_AES_l107;
  wire                when_AES_l125;
  wire                when_AES_l151;
  wire                when_AES_l188;
  wire                when_AES_l195;
  wire                when_AES_l169;
  wire                when_AES_l201;
  wire                when_AES_l218;
  wire                when_AES_l237;
  wire                when_AES_l264;
  wire                when_AES_l274;
  wire                io_ciphertext_fire;
  wire                when_AES_l286;
  `ifndef SYNTHESIS
  reg [47:0] r_aesState_string;
  `endif


  assign _zz_r_round_valueNext_1 = r_round_willIncrement;
  assign _zz_r_round_valueNext = {3'd0, _zz_r_round_valueNext_1};
  assign _zz_r_control_valueNext_1 = r_control_willIncrement;
  assign _zz_r_control_valueNext = {4'd0, _zz_r_control_valueNext_1};
  assign _zz_r_roundConstant = (r_roundConstant <<< 1);
  assign _zz_io_ciphertext_payload_1 = r_control_value[1 : 0];
  assign _zz_io_ciphertext_payload_3 = r_control_value[1 : 0];
  assign _zz_io_ciphertext_payload_5 = r_control_value[1 : 0];
  assign _zz_io_ciphertext_payload_7 = r_control_value[1 : 0];
  AddRoundKey r_addroundkey (
    .io_a    (r_stateReg_0_0[7:0]      ), //i
    .io_k    (r_keyReg_0_0[7:0]        ), //i
    .io_b    (r_addroundkey_io_b[7:0]  )  //o
  );
  CanrightSBox r_sbox (
    .io_subBytesInput     (r_sbox_io_subBytesInput[7:0]   ), //i
    .io_subBytesOutput    (r_sbox_io_subBytesOutput[7:0]  ), //o
    .io_clk               (io_clk                         ), //i
    .io_reset             (io_reset                       )  //i
  );
  MixColumns r_mixcolumns (
    .io_mcIn1     (r_stateReg_0_0[7:0]          ), //i
    .io_mcIn2     (r_stateReg_1_0[7:0]          ), //i
    .io_mcIn3     (r_stateReg_2_0[7:0]          ), //i
    .io_mcIn4     (r_stateReg_3_0[7:0]          ), //i
    .io_mcOut1    (r_mixcolumns_io_mcOut1[7:0]  ), //o
    .io_mcOut2    (r_mixcolumns_io_mcOut2[7:0]  ), //o
    .io_mcOut3    (r_mixcolumns_io_mcOut3[7:0]  ), //o
    .io_mcOut4    (r_mixcolumns_io_mcOut4[7:0]  )  //o
  );
  always @(*) begin
    case(_zz_io_ciphertext_payload_1)
      2'b00 : begin
        _zz_io_ciphertext_payload = r_stateReg_0_0;
      end
      2'b01 : begin
        _zz_io_ciphertext_payload = r_stateReg_0_1;
      end
      2'b10 : begin
        _zz_io_ciphertext_payload = r_stateReg_0_2;
      end
      default : begin
        _zz_io_ciphertext_payload = r_stateReg_0_3;
      end
    endcase
  end

  always @(*) begin
    case(_zz_io_ciphertext_payload_3)
      2'b00 : begin
        _zz_io_ciphertext_payload_2 = r_stateReg_1_0;
      end
      2'b01 : begin
        _zz_io_ciphertext_payload_2 = r_stateReg_1_1;
      end
      2'b10 : begin
        _zz_io_ciphertext_payload_2 = r_stateReg_1_2;
      end
      default : begin
        _zz_io_ciphertext_payload_2 = r_stateReg_1_3;
      end
    endcase
  end

  always @(*) begin
    case(_zz_io_ciphertext_payload_5)
      2'b00 : begin
        _zz_io_ciphertext_payload_4 = r_stateReg_2_0;
      end
      2'b01 : begin
        _zz_io_ciphertext_payload_4 = r_stateReg_2_1;
      end
      2'b10 : begin
        _zz_io_ciphertext_payload_4 = r_stateReg_2_2;
      end
      default : begin
        _zz_io_ciphertext_payload_4 = r_stateReg_2_3;
      end
    endcase
  end

  always @(*) begin
    case(_zz_io_ciphertext_payload_7)
      2'b00 : begin
        _zz_io_ciphertext_payload_6 = r_stateReg_3_0;
      end
      2'b01 : begin
        _zz_io_ciphertext_payload_6 = r_stateReg_3_1;
      end
      2'b10 : begin
        _zz_io_ciphertext_payload_6 = r_stateReg_3_2;
      end
      default : begin
        _zz_io_ciphertext_payload_6 = r_stateReg_3_3;
      end
    endcase
  end

  `ifndef SYNTHESIS
  always @(*) begin
    case(r_aesState)
      States2_sIdle : r_aesState_string = "sIdle ";
      States2_sInit : r_aesState_string = "sInit ";
      States2_sRound : r_aesState_string = "sRound";
      States2_sDone : r_aesState_string = "sDone ";
      default : r_aesState_string = "??????";
    endcase
  end
  `endif

  always @(*) begin
    r_round_willIncrement = 1'b0;
    if(r_control_willOverflow) begin
      r_round_willIncrement = 1'b1;
    end
  end

  always @(*) begin
    r_round_willClear = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
        r_round_willClear = 1'b1;
      end
      States2_sInit : begin
      end
      States2_sRound : begin
      end
      default : begin
      end
    endcase
  end

  assign r_round_willOverflowIfInc = (r_round_value == 4'b1011);
  assign r_round_willOverflow = (r_round_willOverflowIfInc && r_round_willIncrement);
  always @(*) begin
    if(r_round_willOverflow) begin
      r_round_valueNext = 4'b0001;
    end else begin
      r_round_valueNext = (r_round_value + _zz_r_round_valueNext);
    end
    if(r_round_willClear) begin
      r_round_valueNext = 4'b0001;
    end
  end

  always @(*) begin
    r_control_willIncrement = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
      end
      States2_sInit : begin
        r_control_willIncrement = 1'b1;
      end
      States2_sRound : begin
        r_control_willIncrement = 1'b1;
      end
      default : begin
        if(io_ciphertext_fire) begin
          r_control_willIncrement = 1'b1;
        end
      end
    endcase
  end

  always @(*) begin
    r_control_willClear = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
        r_control_willClear = 1'b1;
      end
      States2_sInit : begin
        if(when_AES_l264) begin
          r_control_willClear = 1'b1;
        end
      end
      States2_sRound : begin
        if(when_AES_l274) begin
          r_control_willClear = 1'b1;
        end
      end
      default : begin
      end
    endcase
  end

  assign r_control_willOverflowIfInc = (r_control_value == 5'h14);
  assign r_control_willOverflow = (r_control_willOverflowIfInc && r_control_willIncrement);
  always @(*) begin
    if(r_control_willOverflow) begin
      r_control_valueNext = 5'h0;
    end else begin
      r_control_valueNext = (r_control_value + _zz_r_control_valueNext);
    end
    if(r_control_willClear) begin
      r_control_valueNext = 5'h0;
    end
  end

  assign r_finalKeyAdd = (r_round_value == 4'b1011);
  assign r_finalRound = (r_round_value == 4'b1010);
  assign when_AES_l44 = (r_control_value < 5'h10);
  always @(*) begin
    if(when_AES_l44) begin
      r_sbox_io_subBytesInput = r_addroundkey_io_b;
    end else begin
      r_sbox_io_subBytesInput = r_keyReg_1_3;
    end
  end

  assign when_AES_l56 = (r_control_value == 5'h11);
  always @(*) begin
    if(when_AES_l56) begin
      r_rc = r_roundConstant;
    end else begin
      r_rc = 8'h0;
    end
  end

  assign when_AES_l62 = (r_aesState == States2_sInit);
  assign when_AES_l80 = (r_aesState == States2_sDone);
  assign when_AES_l86 = ((r_control_value < 5'h10) && (r_aesState != States2_sDone));
  assign when_AES_l107 = (r_control_value == 5'h10);
  assign when_AES_l125 = (5'h10 < r_control_value);
  assign when_AES_l151 = (r_aesState == States2_sInit);
  assign when_AES_l188 = (r_control_value < 5'h0c);
  assign when_AES_l195 = (r_round_value == 4'b0001);
  assign when_AES_l169 = (r_control_value < 5'h10);
  assign when_AES_l201 = (((r_control_value == 5'h10) && (r_aesState == States2_sRound)) && (4'b0001 < r_round_value));
  assign when_AES_l218 = (5'h11 <= r_control_value);
  always @(*) begin
    io_plaintext_ready = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
      end
      States2_sInit : begin
        io_plaintext_ready = 1'b1;
      end
      States2_sRound : begin
      end
      default : begin
      end
    endcase
  end

  always @(*) begin
    io_key_ready = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
      end
      States2_sInit : begin
        io_key_ready = 1'b1;
      end
      States2_sRound : begin
      end
      default : begin
      end
    endcase
  end

  always @(*) begin
    io_ciphertext_valid = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
      end
      States2_sInit : begin
      end
      States2_sRound : begin
      end
      default : begin
        io_ciphertext_valid = 1'b1;
      end
    endcase
  end

  assign io_ciphertext_payload = {{{_zz_io_ciphertext_payload,_zz_io_ciphertext_payload_2},_zz_io_ciphertext_payload_4},_zz_io_ciphertext_payload_6};
  always @(*) begin
    io_done = 1'b0;
    case(r_aesState)
      States2_sIdle : begin
      end
      States2_sInit : begin
      end
      States2_sRound : begin
      end
      default : begin
        io_done = 1'b1;
      end
    endcase
  end

  assign when_AES_l237 = (r_roundConstant < 8'h80);
  assign when_AES_l264 = (r_control_value == 5'h03);
  assign when_AES_l274 = ((r_round_value == 4'b1011) && (r_control_value == 5'h0f));
  assign io_ciphertext_fire = (io_ciphertext_valid && io_ciphertext_ready);
  assign when_AES_l286 = (r_control_value == 5'h03);
  always @(posedge io_clk or posedge io_reset) begin
    if(io_reset) begin
      r_stateReg_0_0 <= 8'h0;
      r_stateReg_0_1 <= 8'h0;
      r_stateReg_0_2 <= 8'h0;
      r_stateReg_0_3 <= 8'h0;
      r_stateReg_1_0 <= 8'h0;
      r_stateReg_1_1 <= 8'h0;
      r_stateReg_1_2 <= 8'h0;
      r_stateReg_1_3 <= 8'h0;
      r_stateReg_2_0 <= 8'h0;
      r_stateReg_2_1 <= 8'h0;
      r_stateReg_2_2 <= 8'h0;
      r_stateReg_2_3 <= 8'h0;
      r_stateReg_3_0 <= 8'h0;
      r_stateReg_3_1 <= 8'h0;
      r_stateReg_3_2 <= 8'h0;
      r_stateReg_3_3 <= 8'h0;
      r_keyReg_0_0 <= 8'h0;
      r_keyReg_0_1 <= 8'h0;
      r_keyReg_0_2 <= 8'h0;
      r_keyReg_0_3 <= 8'h0;
      r_keyReg_1_0 <= 8'h0;
      r_keyReg_1_1 <= 8'h0;
      r_keyReg_1_2 <= 8'h0;
      r_keyReg_1_3 <= 8'h0;
      r_keyReg_2_0 <= 8'h0;
      r_keyReg_2_1 <= 8'h0;
      r_keyReg_2_2 <= 8'h0;
      r_keyReg_2_3 <= 8'h0;
      r_keyReg_3_0 <= 8'h0;
      r_keyReg_3_1 <= 8'h0;
      r_keyReg_3_2 <= 8'h0;
      r_keyReg_3_3 <= 8'h0;
      r_aesState <= States2_sIdle;
      r_round_value <= 4'b0001;
      r_control_value <= 5'h0;
      r_roundConstant <= 8'h01;
    end else begin
      r_round_value <= r_round_valueNext;
      r_control_value <= r_control_valueNext;
      if(when_AES_l62) begin
        r_stateReg_0_0 <= r_stateReg_0_1;
        r_stateReg_0_1 <= r_stateReg_0_2;
        r_stateReg_0_2 <= r_stateReg_0_3;
        r_stateReg_0_3 <= io_plaintext_payload[31 : 24];
        r_stateReg_1_0 <= r_stateReg_1_1;
        r_stateReg_1_1 <= r_stateReg_1_2;
        r_stateReg_1_2 <= r_stateReg_1_3;
        r_stateReg_1_3 <= io_plaintext_payload[23 : 16];
        r_stateReg_2_0 <= r_stateReg_2_1;
        r_stateReg_2_1 <= r_stateReg_2_2;
        r_stateReg_2_2 <= r_stateReg_2_3;
        r_stateReg_2_3 <= io_plaintext_payload[15 : 8];
        r_stateReg_3_0 <= r_stateReg_3_1;
        r_stateReg_3_1 <= r_stateReg_3_2;
        r_stateReg_3_2 <= r_stateReg_3_3;
        r_stateReg_3_3 <= io_plaintext_payload[7 : 0];
        if(when_AES_l80) begin
          r_stateReg_0_3 <= r_stateReg_0_0;
          r_stateReg_1_3 <= r_stateReg_1_0;
          r_stateReg_2_3 <= r_stateReg_2_0;
          r_stateReg_3_3 <= r_stateReg_3_0;
        end
      end else begin
        if(when_AES_l86) begin
          r_stateReg_0_0 <= r_stateReg_1_0;
          r_stateReg_1_0 <= r_stateReg_2_0;
          r_stateReg_2_0 <= r_stateReg_3_0;
          r_stateReg_3_0 <= r_stateReg_0_1;
          r_stateReg_0_1 <= r_stateReg_1_1;
          r_stateReg_1_1 <= r_stateReg_2_1;
          r_stateReg_2_1 <= r_stateReg_3_1;
          r_stateReg_3_1 <= r_stateReg_0_2;
          r_stateReg_0_2 <= r_stateReg_1_2;
          r_stateReg_1_2 <= r_stateReg_2_2;
          r_stateReg_2_2 <= r_stateReg_3_2;
          r_stateReg_3_2 <= r_stateReg_0_3;
          r_stateReg_0_3 <= r_stateReg_1_3;
          r_stateReg_1_3 <= r_stateReg_2_3;
          r_stateReg_2_3 <= r_stateReg_3_3;
          r_stateReg_3_3 <= r_sbox_io_subBytesOutput;
          if(r_finalKeyAdd) begin
            r_stateReg_3_3 <= r_addroundkey_io_b;
          end
        end else begin
          if(when_AES_l107) begin
            r_stateReg_0_0 <= r_stateReg_0_0;
            r_stateReg_0_1 <= r_stateReg_0_1;
            r_stateReg_0_2 <= r_stateReg_0_2;
            r_stateReg_0_3 <= r_stateReg_0_3;
            r_stateReg_1_0 <= r_stateReg_1_1;
            r_stateReg_1_1 <= r_stateReg_1_2;
            r_stateReg_1_2 <= r_stateReg_1_3;
            r_stateReg_1_3 <= r_stateReg_1_0;
            r_stateReg_2_0 <= r_stateReg_2_2;
            r_stateReg_2_1 <= r_stateReg_2_3;
            r_stateReg_2_2 <= r_stateReg_2_0;
            r_stateReg_2_3 <= r_stateReg_2_1;
            r_stateReg_3_0 <= r_stateReg_3_3;
            r_stateReg_3_1 <= r_stateReg_3_0;
            r_stateReg_3_2 <= r_stateReg_3_1;
            r_stateReg_3_3 <= r_stateReg_3_2;
          end else begin
            if(when_AES_l125) begin
              r_stateReg_0_0 <= r_stateReg_0_1;
              r_stateReg_1_0 <= r_stateReg_1_1;
              r_stateReg_2_0 <= r_stateReg_2_1;
              r_stateReg_3_0 <= r_stateReg_3_1;
              r_stateReg_0_1 <= r_stateReg_0_2;
              r_stateReg_1_1 <= r_stateReg_1_2;
              r_stateReg_2_1 <= r_stateReg_2_2;
              r_stateReg_3_1 <= r_stateReg_3_2;
              r_stateReg_0_2 <= r_stateReg_0_3;
              r_stateReg_1_2 <= r_stateReg_1_3;
              r_stateReg_2_2 <= r_stateReg_2_3;
              r_stateReg_3_2 <= r_stateReg_3_3;
              r_stateReg_0_3 <= r_mixcolumns_io_mcOut1;
              r_stateReg_1_3 <= r_mixcolumns_io_mcOut2;
              r_stateReg_2_3 <= r_mixcolumns_io_mcOut3;
              r_stateReg_3_3 <= r_mixcolumns_io_mcOut4;
              if(r_finalRound) begin
                r_stateReg_0_3 <= r_stateReg_0_0;
                r_stateReg_1_3 <= r_stateReg_1_0;
                r_stateReg_2_3 <= r_stateReg_2_0;
                r_stateReg_3_3 <= r_stateReg_3_0;
              end
            end
          end
        end
      end
      if(when_AES_l151) begin
        r_keyReg_0_0 <= r_keyReg_0_1;
        r_keyReg_0_1 <= r_keyReg_0_2;
        r_keyReg_0_2 <= r_keyReg_0_3;
        r_keyReg_0_3 <= io_key_payload[31 : 24];
        r_keyReg_1_0 <= r_keyReg_1_1;
        r_keyReg_1_1 <= r_keyReg_1_2;
        r_keyReg_1_2 <= r_keyReg_1_3;
        r_keyReg_1_3 <= io_key_payload[23 : 16];
        r_keyReg_2_0 <= r_keyReg_2_1;
        r_keyReg_2_1 <= r_keyReg_2_2;
        r_keyReg_2_2 <= r_keyReg_2_3;
        r_keyReg_2_3 <= io_key_payload[15 : 8];
        r_keyReg_3_0 <= r_keyReg_3_1;
        r_keyReg_3_1 <= r_keyReg_3_2;
        r_keyReg_3_2 <= r_keyReg_3_3;
        r_keyReg_3_3 <= io_key_payload[7 : 0];
      end else begin
        if(when_AES_l169) begin
          r_keyReg_0_0 <= r_keyReg_1_0;
          r_keyReg_1_0 <= r_keyReg_2_0;
          r_keyReg_2_0 <= r_keyReg_3_0;
          r_keyReg_0_1 <= r_keyReg_1_1;
          r_keyReg_1_1 <= r_keyReg_2_1;
          r_keyReg_2_1 <= r_keyReg_3_1;
          r_keyReg_0_2 <= r_keyReg_1_2;
          r_keyReg_1_2 <= r_keyReg_2_2;
          r_keyReg_2_2 <= r_keyReg_3_2;
          r_keyReg_0_3 <= r_keyReg_1_3;
          r_keyReg_1_3 <= r_keyReg_2_3;
          r_keyReg_2_3 <= r_keyReg_3_3;
          r_keyReg_3_0 <= r_keyReg_0_0;
          r_keyReg_3_1 <= r_keyReg_0_1;
          r_keyReg_3_2 <= r_keyReg_0_2;
          r_keyReg_3_3 <= r_keyReg_0_3;
          if(when_AES_l188) begin
            r_keyReg_3_0 <= (r_keyReg_0_0 ^ r_keyReg_0_1);
            r_keyReg_3_1 <= r_keyReg_0_2;
            r_keyReg_3_2 <= r_keyReg_0_3;
            r_keyReg_3_3 <= r_keyReg_0_0;
          end
          if(when_AES_l195) begin
            r_keyReg_3_0 <= r_keyReg_0_1;
            r_keyReg_3_1 <= r_keyReg_0_2;
            r_keyReg_3_2 <= r_keyReg_0_3;
            r_keyReg_3_3 <= r_keyReg_0_0;
          end
        end else begin
          if(when_AES_l201) begin
            r_keyReg_0_0 <= r_keyReg_0_1;
            r_keyReg_0_1 <= r_keyReg_0_2;
            r_keyReg_0_2 <= r_keyReg_0_3;
            r_keyReg_0_3 <= r_keyReg_0_0;
            r_keyReg_1_0 <= r_keyReg_1_1;
            r_keyReg_1_1 <= r_keyReg_1_2;
            r_keyReg_1_2 <= r_keyReg_1_3;
            r_keyReg_1_3 <= r_keyReg_1_0;
            r_keyReg_2_0 <= r_keyReg_2_1;
            r_keyReg_2_1 <= r_keyReg_2_2;
            r_keyReg_2_2 <= r_keyReg_2_3;
            r_keyReg_2_3 <= r_keyReg_2_0;
            r_keyReg_3_0 <= r_keyReg_3_1;
            r_keyReg_3_1 <= r_keyReg_3_2;
            r_keyReg_3_2 <= r_keyReg_3_3;
            r_keyReg_3_3 <= r_keyReg_3_0;
          end else begin
            if(when_AES_l218) begin
              r_keyReg_0_0 <= r_keyReg_1_0;
              r_keyReg_1_0 <= r_keyReg_2_0;
              r_keyReg_2_0 <= r_keyReg_3_0;
              r_keyReg_3_0 <= ((r_sbox_io_subBytesOutput ^ r_keyReg_0_0) ^ r_rc);
              r_keyReg_1_3 <= r_keyReg_2_3;
              r_keyReg_2_3 <= r_keyReg_3_3;
              r_keyReg_3_3 <= r_keyReg_0_3;
              r_keyReg_0_3 <= r_keyReg_1_3;
            end
          end
        end
      end
      if(r_control_willOverflow) begin
        if(when_AES_l237) begin
          r_roundConstant <= (r_roundConstant <<< 1);
        end else begin
          r_roundConstant <= (_zz_r_roundConstant ^ 8'h1b);
        end
      end
      case(r_aesState)
        States2_sIdle : begin
          r_aesState <= States2_sIdle;
          r_stateReg_0_0 <= 8'h0;
          r_stateReg_0_1 <= 8'h0;
          r_stateReg_0_2 <= 8'h0;
          r_stateReg_0_3 <= 8'h0;
          r_stateReg_1_0 <= 8'h0;
          r_stateReg_1_1 <= 8'h0;
          r_stateReg_1_2 <= 8'h0;
          r_stateReg_1_3 <= 8'h0;
          r_stateReg_2_0 <= 8'h0;
          r_stateReg_2_1 <= 8'h0;
          r_stateReg_2_2 <= 8'h0;
          r_stateReg_2_3 <= 8'h0;
          r_stateReg_3_0 <= 8'h0;
          r_stateReg_3_1 <= 8'h0;
          r_stateReg_3_2 <= 8'h0;
          r_stateReg_3_3 <= 8'h0;
          r_keyReg_0_0 <= 8'h0;
          r_keyReg_0_1 <= 8'h0;
          r_keyReg_0_2 <= 8'h0;
          r_keyReg_0_3 <= 8'h0;
          r_keyReg_1_0 <= 8'h0;
          r_keyReg_1_1 <= 8'h0;
          r_keyReg_1_2 <= 8'h0;
          r_keyReg_1_3 <= 8'h0;
          r_keyReg_2_0 <= 8'h0;
          r_keyReg_2_1 <= 8'h0;
          r_keyReg_2_2 <= 8'h0;
          r_keyReg_2_3 <= 8'h0;
          r_keyReg_3_0 <= 8'h0;
          r_keyReg_3_1 <= 8'h0;
          r_keyReg_3_2 <= 8'h0;
          r_keyReg_3_3 <= 8'h0;
          r_roundConstant <= 8'h01;
          if(io_enable) begin
            r_aesState <= States2_sInit;
          end
        end
        States2_sInit : begin
          r_aesState <= States2_sInit;
          if(when_AES_l264) begin
            r_aesState <= States2_sRound;
          end
        end
        States2_sRound : begin
          r_aesState <= States2_sRound;
          if(when_AES_l274) begin
            r_aesState <= States2_sDone;
          end
        end
        default : begin
          r_aesState <= States2_sDone;
          if(io_ciphertext_fire) begin
            if(when_AES_l286) begin
              r_aesState <= States2_sIdle;
            end
          end
        end
      endcase
    end
  end


endmodule

module MixColumns (
  input      [7:0]    io_mcIn1,
  input      [7:0]    io_mcIn2,
  input      [7:0]    io_mcIn3,
  input      [7:0]    io_mcIn4,
  output     [7:0]    io_mcOut1,
  output     [7:0]    io_mcOut2,
  output     [7:0]    io_mcOut3,
  output     [7:0]    io_mcOut4
);

  wire       [8:0]    _zz__zz_io_mcOut1;
  wire       [8:0]    _zz__zz_io_mcOut1_1;
  wire       [8:0]    _zz__zz_io_mcOut2;
  wire       [8:0]    _zz__zz_io_mcOut2_1;
  wire       [8:0]    _zz__zz_io_mcOut3;
  wire       [8:0]    _zz__zz_io_mcOut3_1;
  wire       [8:0]    _zz__zz_io_mcOut4;
  wire       [8:0]    _zz__zz_io_mcOut4_1;
  wire       [7:0]    a1;
  wire       [7:0]    a2;
  wire       [7:0]    a3;
  wire       [7:0]    a0;
  reg        [8:0]    _zz_io_mcOut1;
  wire                when_MixColumns_l26;
  reg        [8:0]    _zz_io_mcOut1_1;
  wire                when_MixColumns_l26_1;
  reg        [8:0]    _zz_io_mcOut2;
  wire                when_MixColumns_l26_2;
  reg        [8:0]    _zz_io_mcOut2_1;
  wire                when_MixColumns_l26_3;
  reg        [8:0]    _zz_io_mcOut3;
  wire                when_MixColumns_l26_4;
  reg        [8:0]    _zz_io_mcOut3_1;
  wire                when_MixColumns_l26_5;
  reg        [8:0]    _zz_io_mcOut4;
  wire                when_MixColumns_l26_6;
  reg        [8:0]    _zz_io_mcOut4_1;
  wire                when_MixColumns_l26_7;

  assign _zz__zz_io_mcOut1 = ({1'd0,a0} <<< 1);
  assign _zz__zz_io_mcOut1_1 = ({1'd0,a1} <<< 1);
  assign _zz__zz_io_mcOut2 = ({1'd0,a1} <<< 1);
  assign _zz__zz_io_mcOut2_1 = ({1'd0,a2} <<< 1);
  assign _zz__zz_io_mcOut3 = ({1'd0,a2} <<< 1);
  assign _zz__zz_io_mcOut3_1 = ({1'd0,a3} <<< 1);
  assign _zz__zz_io_mcOut4 = ({1'd0,a0} <<< 1);
  assign _zz__zz_io_mcOut4_1 = ({1'd0,a3} <<< 1);
  assign a0 = io_mcIn1;
  assign a1 = io_mcIn2;
  assign a2 = io_mcIn3;
  assign a3 = io_mcIn4;
  assign when_MixColumns_l26 = a0[7];
  always @(*) begin
    if(when_MixColumns_l26) begin
      _zz_io_mcOut1 = (_zz__zz_io_mcOut1 ^ 9'h11b);
    end else begin
      _zz_io_mcOut1 = ({1'd0,a0} <<< 1);
    end
  end

  assign when_MixColumns_l26_1 = a1[7];
  always @(*) begin
    if(when_MixColumns_l26_1) begin
      _zz_io_mcOut1_1 = (_zz__zz_io_mcOut1_1 ^ 9'h11b);
    end else begin
      _zz_io_mcOut1_1 = ({1'd0,a1} <<< 1);
    end
  end

  assign io_mcOut1 = (((_zz_io_mcOut1[7 : 0] ^ (_zz_io_mcOut1_1[7 : 0] ^ a1)) ^ a2) ^ a3);
  assign when_MixColumns_l26_2 = a1[7];
  always @(*) begin
    if(when_MixColumns_l26_2) begin
      _zz_io_mcOut2 = (_zz__zz_io_mcOut2 ^ 9'h11b);
    end else begin
      _zz_io_mcOut2 = ({1'd0,a1} <<< 1);
    end
  end

  assign when_MixColumns_l26_3 = a2[7];
  always @(*) begin
    if(when_MixColumns_l26_3) begin
      _zz_io_mcOut2_1 = (_zz__zz_io_mcOut2_1 ^ 9'h11b);
    end else begin
      _zz_io_mcOut2_1 = ({1'd0,a2} <<< 1);
    end
  end

  assign io_mcOut2 = (((a0 ^ _zz_io_mcOut2[7 : 0]) ^ (_zz_io_mcOut2_1[7 : 0] ^ a2)) ^ a3);
  assign when_MixColumns_l26_4 = a2[7];
  always @(*) begin
    if(when_MixColumns_l26_4) begin
      _zz_io_mcOut3 = (_zz__zz_io_mcOut3 ^ 9'h11b);
    end else begin
      _zz_io_mcOut3 = ({1'd0,a2} <<< 1);
    end
  end

  assign when_MixColumns_l26_5 = a3[7];
  always @(*) begin
    if(when_MixColumns_l26_5) begin
      _zz_io_mcOut3_1 = (_zz__zz_io_mcOut3_1 ^ 9'h11b);
    end else begin
      _zz_io_mcOut3_1 = ({1'd0,a3} <<< 1);
    end
  end

  assign io_mcOut3 = (((a0 ^ a1) ^ _zz_io_mcOut3[7 : 0]) ^ (_zz_io_mcOut3_1[7 : 0] ^ a3));
  assign when_MixColumns_l26_6 = a0[7];
  always @(*) begin
    if(when_MixColumns_l26_6) begin
      _zz_io_mcOut4 = (_zz__zz_io_mcOut4 ^ 9'h11b);
    end else begin
      _zz_io_mcOut4 = ({1'd0,a0} <<< 1);
    end
  end

  assign when_MixColumns_l26_7 = a3[7];
  always @(*) begin
    if(when_MixColumns_l26_7) begin
      _zz_io_mcOut4_1 = (_zz__zz_io_mcOut4_1 ^ 9'h11b);
    end else begin
      _zz_io_mcOut4_1 = ({1'd0,a3} <<< 1);
    end
  end

  assign io_mcOut4 = ((((_zz_io_mcOut4[7 : 0] ^ a0) ^ a1) ^ a2) ^ _zz_io_mcOut4_1[7 : 0]);

endmodule

module CanrightSBox (
  input      [7:0]    io_subBytesInput,
  output     [7:0]    io_subBytesOutput,
  input               io_clk,
  input               io_reset
);

  wire       [7:0]    convertBasis1_io_output;
  wire       [7:0]    convertBasis2_io_output;
  wire       [7:0]    inv_io_x;
  wire                dummy;
  wire       [7:0]    t;
  wire       [7:0]    x;

  NewBasis convertBasis1 (
    .io_input        (io_subBytesInput[7:0]         ), //i
    .io_direction    (1'b0                          ), //i
    .io_output       (convertBasis1_io_output[7:0]  ), //o
    .io_clk          (io_clk                        ), //i
    .io_reset        (io_reset                      )  //i
  );
  NewBasis convertBasis2 (
    .io_input        (x[7:0]                        ), //i
    .io_direction    (1'b1                          ), //i
    .io_output       (convertBasis2_io_output[7:0]  ), //o
    .io_clk          (io_clk                        ), //i
    .io_reset        (io_reset                      )  //i
  );
  G256Inv inv (
    .io_t    (t[7:0]         ), //i
    .io_x    (inv_io_x[7:0]  )  //o
  );
  assign dummy = 1'b0;
  assign t = convertBasis1_io_output;
  assign x = inv_io_x;
  assign io_subBytesOutput = (convertBasis2_io_output ^ 8'h63);

endmodule

module AddRoundKey (
  input      [7:0]    io_a,
  input      [7:0]    io_k,
  output     [7:0]    io_b
);


  assign io_b = (io_a ^ io_k);

endmodule

module G256Inv (
  input      [7:0]    io_t,
  output     [7:0]    io_x
);

  wire       [3:0]    sqSc_io_t;
  wire       [3:0]    inv_io_t;
  wire       [3:0]    sqSc_io_x;
  wire       [3:0]    mul1_io_x;
  wire       [3:0]    mul2_io_x;
  wire       [3:0]    mul3_io_x;
  wire       [3:0]    inv_io_x;
  wire       [3:0]    a;
  wire       [3:0]    b;
  wire       [3:0]    c;
  wire       [3:0]    d;
  wire       [3:0]    e;
  wire       [3:0]    p;
  wire       [3:0]    q;

  G16SqSc sqSc (
    .io_t    (sqSc_io_t[3:0]  ), //i
    .io_x    (sqSc_io_x[3:0]  )  //o
  );
  G16Mul mul1 (
    .io_a    (a[3:0]          ), //i
    .io_b    (b[3:0]          ), //i
    .io_x    (mul1_io_x[3:0]  )  //o
  );
  G16Mul mul2 (
    .io_a    (e[3:0]          ), //i
    .io_b    (b[3:0]          ), //i
    .io_x    (mul2_io_x[3:0]  )  //o
  );
  G16Mul mul3 (
    .io_a    (e[3:0]          ), //i
    .io_b    (a[3:0]          ), //i
    .io_x    (mul3_io_x[3:0]  )  //o
  );
  G16Inv inv (
    .io_t    (inv_io_t[3:0]  ), //i
    .io_x    (inv_io_x[3:0]  )  //o
  );
  assign sqSc_io_t = (a ^ b);
  assign c = sqSc_io_x;
  assign d = mul1_io_x;
  assign inv_io_t = (c ^ d);
  assign e = inv_io_x;
  assign p = mul2_io_x;
  assign q = mul3_io_x;
  assign a = io_t[7 : 4];
  assign b = io_t[3 : 0];
  assign io_x = {p,q};

endmodule

//NewBasis replaced by NewBasis

module NewBasis (
  input      [7:0]    io_input,
  input               io_direction,
  output reg [7:0]    io_output,
  input               io_clk,
  input               io_reset
);

  wire                dummy;
  wire       [7:0]    a;
  reg        [7:0]    b;
  reg        [7:0]    c;
  wire                when_NewBasis_l35;

  assign dummy = 1'b0;
  assign a = io_input;
  always @(*) begin
    b[7] = (((((a[7] ^ a[6]) ^ a[5]) ^ a[2]) ^ a[1]) ^ a[0]);
    b[6] = (((a[6] ^ a[5]) ^ a[4]) ^ a[0]);
    b[5] = (((a[6] ^ a[5]) ^ a[1]) ^ a[0]);
    b[4] = (((a[7] ^ a[6]) ^ a[5]) ^ a[0]);
    b[3] = ((((a[7] ^ a[4]) ^ a[3]) ^ a[1]) ^ a[0]);
    b[2] = a[0];
    b[1] = ((a[6] ^ a[5]) ^ a[0]);
    b[0] = ((((a[6] ^ a[3]) ^ a[2]) ^ a[1]) ^ a[0]);
  end

  always @(*) begin
    c[7] = (a[5] ^ a[3]);
    c[6] = (a[7] ^ a[3]);
    c[5] = (a[6] ^ a[0]);
    c[4] = ((a[7] ^ a[5]) ^ a[3]);
    c[3] = ((((a[7] ^ a[6]) ^ a[5]) ^ a[4]) ^ a[3]);
    c[2] = ((((a[6] ^ a[5]) ^ a[3]) ^ a[2]) ^ a[0]);
    c[1] = ((a[5] ^ a[4]) ^ a[1]);
    c[0] = ((a[6] ^ a[4]) ^ a[1]);
  end

  assign when_NewBasis_l35 = (! io_direction);
  always @(*) begin
    if(when_NewBasis_l35) begin
      io_output = b;
    end else begin
      io_output = c;
    end
  end


endmodule

module G16Inv (
  input      [3:0]    io_t,
  output     [3:0]    io_x
);

  wire       [1:0]    square1_io_t;
  wire       [1:0]    square2_io_t;
  wire       [1:0]    scale_io_x;
  wire       [1:0]    square1_io_x;
  wire       [1:0]    square2_io_x;
  wire       [1:0]    mul1_io_x;
  wire       [1:0]    mul2_io_x;
  wire       [1:0]    mul3_io_x;
  wire       [1:0]    a;
  wire       [1:0]    b;
  wire       [1:0]    c;
  wire       [1:0]    d;
  wire       [1:0]    e;
  wire       [1:0]    p;
  wire       [1:0]    q;
  wire       [1:0]    x;

  G4ScaleN scale (
    .io_t    (square1_io_x[1:0]  ), //i
    .io_x    (scale_io_x[1:0]    )  //o
  );
  G4Sq square1 (
    .io_t    (square1_io_t[1:0]  ), //i
    .io_x    (square1_io_x[1:0]  )  //o
  );
  G4Sq square2 (
    .io_t    (square2_io_t[1:0]  ), //i
    .io_x    (square2_io_x[1:0]  )  //o
  );
  G4Mul mul1 (
    .io_a    (a[1:0]          ), //i
    .io_b    (b[1:0]          ), //i
    .io_x    (mul1_io_x[1:0]  )  //o
  );
  G4Mul mul2 (
    .io_a    (e[1:0]          ), //i
    .io_b    (b[1:0]          ), //i
    .io_x    (mul2_io_x[1:0]  )  //o
  );
  G4Mul mul3 (
    .io_a    (e[1:0]          ), //i
    .io_b    (a[1:0]          ), //i
    .io_x    (mul3_io_x[1:0]  )  //o
  );
  assign square1_io_t = (a ^ b);
  assign c = scale_io_x;
  assign d = mul1_io_x;
  assign square2_io_t = (c ^ d);
  assign e = square2_io_x;
  assign p = mul2_io_x;
  assign q = mul3_io_x;
  assign a = io_t[3 : 2];
  assign b = io_t[1 : 0];
  assign io_x = {p,q};

endmodule

//G16Mul replaced by G16Mul

//G16Mul replaced by G16Mul

module G16Mul (
  input      [3:0]    io_a,
  input      [3:0]    io_b,
  output     [3:0]    io_x
);

  wire       [1:0]    mul1_io_a;
  wire       [1:0]    mul1_io_b;
  wire       [1:0]    mul1_io_x;
  wire       [1:0]    mul2_io_x;
  wire       [1:0]    mul3_io_x;
  wire       [1:0]    scale_io_x;
  wire       [1:0]    a;
  wire       [1:0]    b;
  wire       [1:0]    c;
  wire       [1:0]    d;
  wire       [1:0]    e;
  wire       [1:0]    f;
  wire       [1:0]    p;
  wire       [1:0]    q;

  G4Mul mul1 (
    .io_a    (mul1_io_a[1:0]  ), //i
    .io_b    (mul1_io_b[1:0]  ), //i
    .io_x    (mul1_io_x[1:0]  )  //o
  );
  G4Mul mul2 (
    .io_a    (a[1:0]          ), //i
    .io_b    (c[1:0]          ), //i
    .io_x    (mul2_io_x[1:0]  )  //o
  );
  G4Mul mul3 (
    .io_a    (b[1:0]          ), //i
    .io_b    (d[1:0]          ), //i
    .io_x    (mul3_io_x[1:0]  )  //o
  );
  G4ScaleN scale (
    .io_t    (e[1:0]           ), //i
    .io_x    (scale_io_x[1:0]  )  //o
  );
  assign mul1_io_a = (a ^ b);
  assign mul1_io_b = (c ^ d);
  assign e = mul1_io_x;
  assign f = scale_io_x;
  assign p = (mul2_io_x ^ f);
  assign q = (mul3_io_x ^ f);
  assign a = io_a[3 : 2];
  assign b = io_a[1 : 0];
  assign c = io_b[3 : 2];
  assign d = io_b[1 : 0];
  assign io_x = {p,q};

endmodule

module G16SqSc (
  input      [3:0]    io_t,
  output     [3:0]    io_x
);

  wire       [1:0]    square1_io_t;
  wire       [1:0]    square1_io_x;
  wire       [1:0]    square2_io_x;
  wire       [1:0]    scale_io_x;
  wire       [1:0]    a;
  wire       [1:0]    b;
  wire       [1:0]    c;
  wire       [1:0]    p;
  wire       [1:0]    q;

  G4Sq square1 (
    .io_t    (square1_io_t[1:0]  ), //i
    .io_x    (square1_io_x[1:0]  )  //o
  );
  G4Sq square2 (
    .io_t    (b[1:0]             ), //i
    .io_x    (square2_io_x[1:0]  )  //o
  );
  G4ScaleN2 scale (
    .io_t    (c[1:0]           ), //i
    .io_x    (scale_io_x[1:0]  )  //o
  );
  assign square1_io_t = (a ^ b);
  assign p = square1_io_x;
  assign c = square2_io_x;
  assign q = scale_io_x;
  assign a = io_t[3 : 2];
  assign b = io_t[1 : 0];
  assign io_x = {p,q};

endmodule

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

//G4Sq replaced by G4Sq

//G4Sq replaced by G4Sq

//G4ScaleN replaced by G4ScaleN

//G4ScaleN replaced by G4ScaleN

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

//G4ScaleN replaced by G4ScaleN

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

module G4ScaleN (
  input      [1:0]    io_t,
  output     [1:0]    io_x
);


  assign io_x = {io_t[0],(io_t[1] ^ io_t[0])};

endmodule

//G4Mul replaced by G4Mul

//G4Mul replaced by G4Mul

module G4Mul (
  input      [1:0]    io_a,
  input      [1:0]    io_b,
  output     [1:0]    io_x
);

  wire       [0:0]    a;
  wire       [0:0]    b;
  wire       [0:0]    c;
  wire       [0:0]    d;
  wire       [0:0]    e;
  wire       [0:0]    p;
  wire       [0:0]    q;

  assign a = io_a[1];
  assign b = io_a[0];
  assign c = io_b[1];
  assign d = io_b[0];
  assign e = ((a ^ b) & (c ^ d));
  assign p = ((a & c) ^ e);
  assign q = ((b & d) ^ e);
  assign io_x = {p,q};

endmodule

module G4ScaleN2 (
  input      [1:0]    io_t,
  output     [1:0]    io_x
);


  assign io_x = {(io_t[1] ^ io_t[0]),io_t[1]};

endmodule

//G4Sq replaced by G4Sq

module G4Sq (
  input      [1:0]    io_t,
  output     [1:0]    io_x
);


  assign io_x = {io_t[0],io_t[1]};

endmodule
