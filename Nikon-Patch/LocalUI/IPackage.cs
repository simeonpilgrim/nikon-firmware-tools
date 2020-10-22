
namespace Nikon_Patch
{
    public interface IPackage
    {
        bool LoadData(byte[] data);
        bool TryOpen();
        bool PatchCheck(int block, int start, byte[] orig);
        void Patch(int block, int start, byte[] data);
        void Repackage(System.IO.Stream outstream);
    }
}
