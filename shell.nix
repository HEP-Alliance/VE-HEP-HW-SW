let
  inherit (import ./riscv-nix) sources pkgs packages;
in
  pkgs.mkShell {
    nativeBuildInputs =
      (pkgs.lib.remove pkgs.openocd (packages pkgs))
      ++
      (with pkgs; [
        # Required for FstWave (GtkWave but compressed)
        zlib.dev
      ]);
    shellHook = ''
      export RISCV_NAME=riscv32-none-elf
    '';
  }
