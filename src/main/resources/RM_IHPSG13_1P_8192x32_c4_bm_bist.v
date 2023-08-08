// ------------------------------------------------------
//
//
//
//		RacyICs Memory Generator - RI MGene
//
//		DD/MM/YYYY Release Version
//		19/05/2016 Preliminary Release 1.0
//		25/08/2016 Software Release 1.0
//		09/09/2016 Software Release 1.0.1
//		28/09/2016 Software Release 1.0.2
//		05/10/2016 Software Release 1.0.3
//		06/10/2016 Software Release 1.0.4
//		01/12/2016 Software Release 1.0.5
//		01/12/2016 Software Release 1.0.6
//		09/02/2017 Software Release 1.0.7
//		17/02/2017 Software Release 1.0.8
//		09/03/2017 Software Release 1.0.9
//		10/08/2017 Software Release 1.0.10
//
//		Software Versions
//		RI MGene Memory Generator 1.0.10
//		RI MGene SRAM Explorer GUI 1.0
//		RI MGene Database Version 1.0.10082017
//
//		PDK Version
//		IHP SG13S rev1.0.2_a_150421
//		Generated on Tue Jun 14 11:44:35 2022		
//
// ------------------------------------------------------ 
`celldefine
module RM_IHPSG13_1P_8192x32_c4_bm_bist (
    A_CLK,
    A_MEN,
    A_WEN,
    A_REN,
    A_ADDR,
    A_DIN,
    A_DLY,
    A_DOUT,
    A_BM,
    A_BIST_CLK,
    A_BIST_EN,
    A_BIST_MEN,
    A_BIST_WEN,
    A_BIST_REN,
    A_BIST_ADDR,
    A_BIST_DIN,
    A_BIST_BM
);

    input A_CLK;
    input A_MEN;
    input A_WEN;
    input A_REN;
    input [12:0] A_ADDR;
    input [31:0] A_DIN;
    input A_DLY;
    output [31:0] A_DOUT;
    input [31:0] A_BM;
    input A_BIST_CLK;
    input A_BIST_EN;
    input A_BIST_MEN;
    input A_BIST_WEN;
    input A_BIST_REN;
    input [12:0] A_BIST_ADDR;
    input [31:0] A_BIST_DIN;
    input [31:0] A_BIST_BM;

`define FUNCTIONAL
`ifdef FUNCTIONAL  //  functional //


    SRAM_1P_behavioral_bm_bist #(
	.P_DATA_WIDTH(32),
	.P_ADDR_WIDTH(13)
	) i_SRAM_1P_behavioral_bm_bist (
                    .A_CLK(A_CLK),
                    .A_MEN(A_MEN),
                    .A_WEN(A_WEN),
                    .A_REN(A_REN),
                    .A_ADDR(A_ADDR),
                    .A_DLY(A_DLY),
                    .A_DIN(A_DIN),
                    .A_DOUT(A_DOUT), 
                    .A_BM(A_BM), 
                    .A_BIST_CLK(A_BIST_CLK),
                    .A_BIST_EN(A_BIST_EN),
                    .A_BIST_MEN(A_BIST_MEN),
                    .A_BIST_WEN(A_BIST_WEN),
                    .A_BIST_REN(A_BIST_REN),
                    .A_BIST_ADDR(A_BIST_ADDR),
                    .A_BIST_DIN(A_BIST_DIN), 
                    .A_BIST_BM(A_BIST_BM)
		);

`else

    wire A_CLK_DELAY;
    wire A_MEN_DELAY;
    wire A_WEN_DELAY;
    wire A_REN_DELAY;
    wire [12:0] A_ADDR_DELAY;
    wire [31:0] A_DIN_DELAY;
    wire [31:0] A_BM_DELAY;
    wire A_BIST_CLK_DELAY;
    wire A_BIST_MEN_DELAY;
    wire A_BIST_WEN_DELAY;
    wire A_BIST_REN_DELAY;
    wire [12:0] A_BIST_ADDR_DELAY;
    wire [31:0] A_BIST_DIN_DELAY;
    wire [31:0] A_BIST_BM_DELAY;

    reg notifier;

    wire A_RW_ACCESS = (A_WEN || A_REN) && A_MEN;
    wire A_W_ACCESS  = A_WEN && A_MEN;
    wire A_BIST_RW_ACCESS = (A_BIST_WEN || A_BIST_REN) && A_BIST_MEN;
    wire A_BIST_W_ACCESS  = A_BIST_WEN && A_BIST_MEN;



    SRAM_1P_behavioral_bm_bist #(
	.P_DATA_WIDTH(32),
	.P_ADDR_WIDTH(13)
	) i_SRAM_1P_behavioral_bm_bist (
                    .A_CLK(A_CLK_DELAY),
                    .A_MEN(A_MEN_DELAY),
                    .A_WEN(A_WEN_DELAY),
                    .A_REN(A_REN_DELAY),
                    .A_ADDR(A_ADDR_DELAY),
                    .A_DLY(A_DLY),
                    .A_DIN(A_DIN_DELAY),
                    .A_DOUT(A_DOUT), 
                    .A_BM(A_BM_DELAY), 
                    .A_BIST_CLK(A_BIST_CLK_DELAY),
                    .A_BIST_EN(A_BIST_EN),
                    .A_BIST_MEN(A_BIST_MEN_DELAY),
                    .A_BIST_WEN(A_BIST_WEN_DELAY),
                    .A_BIST_REN(A_BIST_REN_DELAY),
                    .A_BIST_ADDR(A_BIST_ADDR_DELAY),
                    .A_BIST_DIN(A_BIST_DIN_DELAY), 
                    .A_BIST_BM(A_BIST_BM_DELAY)
		);


    specify

      (posedge A_CLK *> (A_DOUT : A_DIN)) = (1.0, 1.0);
      $width(posedge A_CLK, 1.0,0,notifier);
      $setuphold(posedge A_CLK &&& A_MEN, posedge A_MEN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_MEN_DELAY);
      $setuphold(posedge A_CLK &&& A_MEN, posedge A_REN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_REN_DELAY);
      $setuphold(posedge A_CLK &&& A_MEN, posedge A_WEN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_WEN_DELAY);
      $setuphold(posedge A_CLK &&& A_MEN, negedge A_MEN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_MEN_DELAY);
      $setuphold(posedge A_CLK &&& A_MEN, negedge A_REN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_REN_DELAY);
      $setuphold(posedge A_CLK &&& A_MEN, negedge A_WEN, 1.0, 1.0,notifier,,,A_CLK_DELAY, A_WEN_DELAY);
      $setuphold(posedge A_CLK &&& A_RW_ACCESS, posedge A_ADDR, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_ADDR_DELAY);
      $setuphold(posedge A_CLK &&& A_RW_ACCESS, negedge A_ADDR, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_ADDR_DELAY);

      $setuphold(posedge A_CLK &&& A_W_ACCESS, posedge A_DIN, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_DIN_DELAY);
      $setuphold(posedge A_CLK &&& A_W_ACCESS, negedge A_DIN, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_DIN_DELAY);
      $setuphold(posedge A_CLK &&& A_W_ACCESS, posedge A_BM, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_BM_DELAY);
      $setuphold(posedge A_CLK &&& A_W_ACCESS, negedge A_BM, 1.0 ,1.0, notifier,,,A_CLK_DELAY, A_BM_DELAY);
      (posedge A_BIST_CLK *> (A_DOUT : A_BIST_DIN)) = (1.0, 1.0);
      $width(posedge A_BIST_CLK, 1.0,0,notifier);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, posedge A_BIST_MEN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_MEN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, posedge A_BIST_REN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_REN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, posedge A_BIST_WEN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_WEN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, negedge A_BIST_MEN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_MEN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, negedge A_BIST_REN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_REN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_MEN, negedge A_BIST_WEN, 1.0, 1.0,notifier,,,A_BIST_CLK_DELAY, A_BIST_WEN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_RW_ACCESS, posedge A_BIST_ADDR, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_ADDR_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_RW_ACCESS, negedge A_BIST_ADDR, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_ADDR_DELAY);

      $setuphold(posedge A_BIST_CLK &&& A_BIST_W_ACCESS, posedge A_BIST_DIN, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_DIN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_W_ACCESS, negedge A_BIST_DIN, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_DIN_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_W_ACCESS, posedge A_BIST_BM, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_BM_DELAY);
      $setuphold(posedge A_BIST_CLK &&& A_BIST_W_ACCESS, negedge A_BIST_BM, 1.0 ,1.0, notifier,,,A_BIST_CLK_DELAY, A_BIST_BM_DELAY);


    endspecify

`endif

endmodule
`endcelldefine
